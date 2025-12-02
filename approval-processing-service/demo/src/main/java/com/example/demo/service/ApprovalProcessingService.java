package com.example.demo.service;

import com.example.demo.dto.ProcessResponse;
import com.example.demo.exception.ApprovalNotFoundException;
import com.example.demo.exception.InvalidStatusException;
import com.example.demo.grpc.ApprovalResultGrpcClient;
import com.example.demo.model.PendingApproval;
import com.example.demo.repository.InMemoryApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 결재 처리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalProcessingService {

    private final InMemoryApprovalRepository repository;
    private final ApprovalResultGrpcClient grpcClient;

    /**
     * 특정 결재자의 대기 중인 결재 목록 조회
     */
    public List<PendingApproval> getPendingApprovals(Long approverId) {
        log.info("결재 대기 목록 조회: approverId={}", approverId);
        List<PendingApproval> pendingApprovals = repository.getPendingApprovals(approverId);
        log.info("조회된 결재 대기 건수: {}", pendingApprovals.size());
        return pendingApprovals;
    }

    /**
     * 결재 처리 (승인/반려)
     */
    public ProcessResponse processApproval(Long approverId, Long requestId, String status) {
        log.info("결재 처리 시작: approverId={}, requestId={}, status={}", approverId, requestId, status);

        // 상태값 검증
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new InvalidStatusException("유효하지 않은 상태값입니다: " + status);
        }

        // 해당 결재자의 대기 목록에서 요청 찾기
        List<PendingApproval> pendingApprovals = repository.getPendingApprovals(approverId);
        PendingApproval targetApproval = pendingApprovals.stream()
                .filter(pa -> pa.getRequestId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new ApprovalNotFoundException(
                        "결재 요청을 찾을 수 없습니다: approverId=" + approverId + ", requestId=" + requestId));

        // In-Memory에서 제거
        repository.removePendingApproval(approverId, requestId);
        log.info("결재 대기 목록에서 제거 완료: approverId={}, requestId={}", approverId, requestId);

        // 현재 결재자의 step 번호 찾기
        int currentStep = targetApproval.getSteps().stream()
                .filter(s -> s.getApproverId().equals(approverId) && "pending".equals(s.getStatus()))
                .findFirst()
                .map(PendingApproval.StepInfo::getStep)
                .orElse(1);

        // gRPC로 Approval Request Service에 결과 전송
        String grpcResult = grpcClient.returnApprovalResult(requestId, currentStep, approverId, status);
        log.info("gRPC 결과 전송 완료: requestId={}, grpcResult={}", requestId, grpcResult);

        return ProcessResponse.builder()
                .requestId(requestId)
                .approverId(approverId)
                .status(status)
                .message("결재 처리가 완료되었습니다.")
                .build();
    }
}
