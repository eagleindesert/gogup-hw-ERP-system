package com.example.demo.config;

import com.example.demo.grpc.ApprovalRequest;
import com.example.demo.grpc.ApprovalResultGrpcClient;
import com.example.demo.grpc.PendingApprovalsResponse;
import com.example.demo.grpc.Step;
import com.example.demo.model.PendingApproval;
import com.example.demo.repository.InMemoryApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 서버 시작 시 Request Service에서 pending 결재 목록을 Pull
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalDataInitializer {

    private final ApprovalResultGrpcClient grpcClient;
    private final InMemoryApprovalRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("서버 시작 - Request Service에서 pending 결재 목록 동기화 시작");
        
        try {
            syncPendingApprovals();
        } catch (Exception e) {
            log.warn("pending 결재 동기화 실패 (Request Service가 아직 시작되지 않았을 수 있음): {}", e.getMessage());
        }
    }

    /**
     * Request Service에서 pending 결재 목록 동기화
     */
    public void syncPendingApprovals() {
        // 기존 데이터 초기화
        repository.clear();
        
        // gRPC로 pending 목록 조회
        PendingApprovalsResponse response = grpcClient.getAllPendingApprovals();
        
        int syncCount = 0;
        for (ApprovalRequest approval : response.getApprovalsList()) {
            // 첫 번째 pending 상태의 결재자 찾기
            Optional<Step> pendingStep = approval.getStepsList().stream()
                    .filter(s -> "pending".equals(s.getStatus()))
                    .findFirst();

            if (pendingStep.isPresent()) {
                Long approverId = (long) pendingStep.get().getApproverId();

                // PendingApproval 객체 생성
                List<PendingApproval.StepInfo> steps = approval.getStepsList().stream()
                        .map(s -> PendingApproval.StepInfo.builder()
                                .step(s.getStep())
                                .approverId((long) s.getApproverId())
                                .status(s.getStatus())
                                .build())
                        .toList();

                PendingApproval pendingApproval = PendingApproval.builder()
                        .requestId((long) approval.getRequestId())
                        .requesterId((long) approval.getRequesterId())
                        .title(approval.getTitle())
                        .content(approval.getContent())
                        .steps(steps)
                        .build();

                // In-Memory에 저장
                repository.addPendingApproval(approverId, pendingApproval);
                syncCount++;
            }
        }
        
        log.info("pending 결재 동기화 완료: {}건", syncCount);
    }
}
