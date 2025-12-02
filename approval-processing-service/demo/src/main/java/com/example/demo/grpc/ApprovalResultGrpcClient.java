package com.example.demo.grpc;

import com.example.demo.grpc.ApprovalResultRequest;
import com.example.demo.grpc.ApprovalResultResponse;
import com.example.demo.grpc.ApprovalServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * gRPC Client - Approval Request Service로 결재 결과 전송
 */
@Slf4j
@Component
public class ApprovalResultGrpcClient {

    @Value("${grpc.client.approval-request-service.address:static://localhost:9091}")
    private String serverAddress;

    private ApprovalServiceGrpc.ApprovalServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        String address = serverAddress.replace("static://", "");
        String[] parts = address.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = ApprovalServiceGrpc.newBlockingStub(channel);
        log.info("gRPC Client 초기화 완료: {}:{}", host, port);
    }

    /**
     * Approval Request Service로 결재 결과 전송
     */
    public String returnApprovalResult(Long requestId, Integer step, Long approverId, String status) {
        try {
            ApprovalResultRequest request = ApprovalResultRequest.newBuilder()
                    .setRequestId(requestId.intValue())
                    .setStep(step)
                    .setApproverId(approverId.intValue())
                    .setStatus(status)
                    .build();

            ApprovalResultResponse response = blockingStub.returnApprovalResult(request);
            log.info("gRPC ReturnApprovalResult 응답: status={}", response.getStatus());
            return response.getStatus();
        } catch (Exception e) {
            log.error("gRPC ReturnApprovalResult 호출 실패: requestId={}", requestId, e);
            return "FAILED";
        }
    }
}
