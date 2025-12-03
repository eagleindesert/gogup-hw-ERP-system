package com.example.demo.grpc;

import com.example.demo.service.ApprovalRequestService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ApprovalResultGrpcServer extends ApprovalServiceGrpc.ApprovalServiceImplBase {

    private final ApprovalRequestService approvalRequestService;

    /**
     * Approval Processing Service로부터 결재 결과를 수신
     */
    @Override
    public void returnApprovalResult(ApprovalResultRequest request,
                                     StreamObserver<ApprovalResultResponse> responseObserver) {
        log.info("gRPC ReturnApprovalResult 수신: requestId={}, step={}, approverId={}, status={}",
                request.getRequestId(), request.getStep(), request.getApproverId(), request.getStatus());

        try {
            approvalRequestService.processApprovalResult(
                    (long) request.getRequestId(),
                    request.getStep(),
                    (long) request.getApproverId(),
                    request.getStatus()
            );

            ApprovalResultResponse response = ApprovalResultResponse.newBuilder()
                    .setStatus("processed")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("결재 결과 처리 실패: requestId={}", request.getRequestId(), e);
            ApprovalResultResponse response = ApprovalResultResponse.newBuilder()
                    .setStatus("error: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * RequestApproval은 이 서비스에서 구현하지 않음 (클라이언트로만 사용)
     */
    @Override
    public void requestApproval(ApprovalRequest request,
                                StreamObserver<ApprovalResponse> responseObserver) {
        ApprovalResponse response = ApprovalResponse.newBuilder()
                .setStatus("not_implemented")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Processing Service가 시작할 때 모든 pending 결재 목록 조회
     */
    @Override
    public void getAllPendingApprovals(EmptyRequest request,
                                       StreamObserver<PendingApprovalsResponse> responseObserver) {
        log.info("gRPC GetAllPendingApprovals 수신 - pending 상태의 모든 결재 목록 조회");

        try {
            var pendingApprovals = approvalRequestService.getAllPendingApprovals();
            
            PendingApprovalsResponse.Builder responseBuilder = PendingApprovalsResponse.newBuilder();
            
            for (var approval : pendingApprovals) {
                ApprovalRequest.Builder approvalBuilder = ApprovalRequest.newBuilder()
                        .setRequestId(approval.getRequestId().intValue())
                        .setRequesterId(approval.getRequesterId().intValue())
                        .setTitle(approval.getTitle())
                        .setContent(approval.getContent() != null ? approval.getContent() : "");
                
                for (var step : approval.getSteps()) {
                    Step grpcStep = Step.newBuilder()
                            .setStep(step.getStep())
                            .setApproverId(step.getApproverId().intValue())
                            .setStatus(step.getStatus())
                            .build();
                    approvalBuilder.addSteps(grpcStep);
                }
                
                responseBuilder.addApprovals(approvalBuilder.build());
            }
            
            log.info("pending 결재 {}건 반환", pendingApprovals.size());
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("pending 결재 목록 조회 실패", e);
            responseObserver.onNext(PendingApprovalsResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }
}
