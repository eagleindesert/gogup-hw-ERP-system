package com.example.demo.grpc;

import com.example.demo.model.PendingApproval;
import com.example.demo.repository.InMemoryApprovalRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;

/**
 * gRPC Server - Approval Request Service로부터 결재 요청 수신
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ApprovalRequestGrpcServer extends ApprovalServiceGrpc.ApprovalServiceImplBase {

    private final InMemoryApprovalRepository repository;

    /**
     * Approval Request Service로부터 결재 요청을 받아 In-Memory에 저장
     */
    @Override
    public void requestApproval(ApprovalRequest request, StreamObserver<ApprovalResponse> responseObserver) {
        log.info("gRPC RequestApproval 수신: requestId={}, requesterId={}, title={}",
                request.getRequestId(), request.getRequesterId(), request.getTitle());

        // 첫 번째 pending 상태의 결재자 찾기
        Optional<Step> pendingStep = request.getStepsList().stream()
                .filter(s -> "pending".equals(s.getStatus()))
                .findFirst();

        if (pendingStep.isPresent()) {
            Long approverId = (long) pendingStep.get().getApproverId();

            // PendingApproval 객체 생성
            List<PendingApproval.StepInfo> steps = request.getStepsList().stream()
                    .map(s -> PendingApproval.StepInfo.builder()
                            .step(s.getStep())
                            .approverId((long) s.getApproverId())
                            .status(s.getStatus())
                            .build())
                    .toList();

            PendingApproval pendingApproval = PendingApproval.builder()
                    .requestId((long) request.getRequestId())
                    .requesterId((long) request.getRequesterId())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .steps(steps)
                    .build();

            // In-Memory에 저장
            repository.addPendingApproval(approverId, pendingApproval);
            log.info("결재 대기 추가 완료: approverId={}, requestId={}", approverId, request.getRequestId());
        } else {
            log.warn("pending 상태의 결재 단계가 없습니다: requestId={}", request.getRequestId());
        }

        // 응답 반환
        ApprovalResponse response = ApprovalResponse.newBuilder()
                .setStatus("received")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * ReturnApprovalResult는 이 서비스에서 사용하지 않음
     * (Approval Request Service가 구현)
     */
    @Override
    public void returnApprovalResult(ApprovalResultRequest request, StreamObserver<ApprovalResultResponse> responseObserver) {
        log.warn("ReturnApprovalResult는 이 서비스에서 지원하지 않습니다.");
        ApprovalResultResponse response = ApprovalResultResponse.newBuilder()
                .setStatus("not_supported")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
