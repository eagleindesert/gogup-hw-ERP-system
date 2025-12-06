package com.example.demo.service;

import com.example.demo.client.EmployeeServiceClient;
import com.example.demo.client.NotificationServiceClient;
import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.document.ApprovalStep;
import com.example.demo.dto.*;
import com.example.demo.exception.ApprovalNotFoundException;
import com.example.demo.exception.InvalidStepOrderException;
import com.example.demo.grpc.ApprovalProcessingGrpcClient;
import com.example.demo.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final EmployeeServiceClient employeeServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ApprovalProcessingGrpcClient approvalProcessingGrpcClient;

    /**
     * 결재 요청 생성
     */
    public Long createApproval(ApprovalCreateRequest request) {
        // 1. 요청자 검증
        employeeServiceClient.validateEmployee(request.getRequesterId());

        // 2. 모든 결재자 검증
        for (StepRequest step : request.getSteps()) {
            employeeServiceClient.validateEmployee(step.getApproverId());
        }

        // 3. 결재 단계 순서 검증 (1부터 오름차순)
        validateStepOrder(request.getSteps());

        // 4. 새 requestId 생성
        Long newRequestId = generateRequestId();

        // 5. Document 생성
        List<ApprovalStep> steps = request.getSteps().stream()
                .map(s -> ApprovalStep.builder()
                        .step(s.getStep())
                        .approverId(s.getApproverId())
                        .status("pending")
                        .build())
                .toList();

        ApprovalRequestDocument document = ApprovalRequestDocument.builder()
                .requestId(newRequestId)
                .requesterId(request.getRequesterId())
                .title(request.getTitle())
                .content(request.getContent())
                .steps(steps)
                .finalStatus("in_progress")
                .createdAt(LocalDateTime.now())
                .build();

        // 6. MongoDB에 저장
        approvalRequestRepository.save(document);
        log.info("결재 요청 생성 완료: requestId={}", newRequestId);

        // 7. gRPC로 Approval Processing Service에 전달
        String grpcResult = approvalProcessingGrpcClient.requestApproval(document);
        log.info("gRPC 전송 결과: {}", grpcResult);

        return newRequestId;
    }

    /**
     * 결재 요청 목록 조회
     */
    public List<ApprovalResponse> getAllApprovals() {
        return approvalRequestRepository.findAll().stream()
                .map(ApprovalResponse::from)
                .toList();
    }

    /**
     * pending 상태인 모든 결재 요청 조회 (gRPC Pull용)
     */
    public List<ApprovalRequestDocument> getAllPendingApprovals() {
        return approvalRequestRepository.findByFinalStatus("in_progress");
    }

    /**
     * 결재 요청 상세 조회
     */
    public ApprovalResponse getApproval(Long requestId) {
        ApprovalRequestDocument document = approvalRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApprovalNotFoundException(requestId));
        return ApprovalResponse.from(document);
    }

    /**
     * 결재 결과 처리 (gRPC 서버에서 호출)
     */
    public void processApprovalResult(Long requestId, int step, Long approverId, String status) {
        ApprovalRequestDocument document = approvalRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ApprovalNotFoundException(requestId));

        // 1. 해당 단계의 상태 업데이트
        LocalDateTime now = LocalDateTime.now();
        for (ApprovalStep s : document.getSteps()) {
            if (s.getStep() == step && s.getApproverId().equals(approverId)) {
                s.setStatus(status);
                s.setUpdatedAt(now);
                break;
            }
        }
        document.setUpdatedAt(now);

        // 2. 반려인 경우
        if ("rejected".equals(status)) {
            document.setFinalStatus("rejected");
            approvalRequestRepository.save(document);
            log.info("결재 반려: requestId={}, step={}, approverId={}", requestId, step, approverId);

            // 요청자에게 반려 알림 전송
            sendRejectionNotification(document, approverId);
            return;
        }

        // 3. 승인인 경우 - 다음 pending 단계 확인
        Optional<ApprovalStep> nextPendingStep = document.getSteps().stream()
                .filter(s -> "pending".equals(s.getStatus()))
                .min(Comparator.comparingInt(ApprovalStep::getStep));

        if (nextPendingStep.isPresent()) {
            // 다음 결재자가 있음 - 저장 후 gRPC로 다시 전달
            approvalRequestRepository.save(document);
            log.info("다음 결재 단계로 이동: requestId={}, nextStep={}", requestId, nextPendingStep.get().getStep());

            // 요청자에게 중간 승인 알림 전송
            sendPartialApprovalNotification(document, step, approverId);

            approvalProcessingGrpcClient.requestApproval(document);
        } else {
            // 모든 단계 완료 - 최종 승인
            document.setFinalStatus("approved");
            approvalRequestRepository.save(document);
            log.info("최종 승인 완료: requestId={}", requestId);

            // 요청자에게 승인 완료 알림 전송
            sendApprovalNotification(document);
        }
    }

    /**
     * 결재 단계 순서 검증
     */
    private void validateStepOrder(List<StepRequest> steps) {
        List<StepRequest> sorted = steps.stream()
                .sorted(Comparator.comparingInt(StepRequest::getStep))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getStep() != i + 1) {
                throw new InvalidStepOrderException();
            }
        }
    }

    /**
     * 새 requestId 생성
     */
    private Long generateRequestId() {
        return approvalRequestRepository.findTopByOrderByRequestIdDesc()
                .map(doc -> doc.getRequestId() + 1)
                .orElse(1L);
    }

    /**
     * 승인 완료 알림 전송
     */
    private void sendApprovalNotification(ApprovalRequestDocument document) {
        NotificationRequest notification = NotificationRequest.builder()
                .requestId(document.getRequestId())
                .employeeId(document.getRequesterId())
                .result("approved")
                .finalResult("approved")
                .build();
        notificationServiceClient.sendNotification(notification);
    }

    /**
     * 중간 단계 승인 알림 전송
     */
    private void sendPartialApprovalNotification(ApprovalRequestDocument document, int step, Long approverId) {
        int totalSteps = document.getSteps().size();
        NotificationRequest notification = NotificationRequest.builder()
                .requestId(document.getRequestId())
                .employeeId(document.getRequesterId())
                .result("approved")
                .currentStep(step)
                .totalSteps(totalSteps)
                .approvedBy(approverId)
                .finalResult("in_progress")
                .build();
        notificationServiceClient.sendNotification(notification);
    }

    /**
     * 반려 알림 전송
     */
    private void sendRejectionNotification(ApprovalRequestDocument document, Long rejectedBy) {
        NotificationRequest notification = NotificationRequest.builder()
                .requestId(document.getRequestId())
                .employeeId(document.getRequesterId())
                .result("rejected")
                .rejectedBy(rejectedBy)
                .finalResult("rejected")
                .build();
        notificationServiceClient.sendNotification(notification);
    }
}
