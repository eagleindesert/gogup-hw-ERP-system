package com.example.demo.grpc;

import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.document.ApprovalStep;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ApprovalProcessingGrpcClient {

    @Value("${grpc.client.approval-processing-service.address:static://localhost:9090}")
    private String serverAddress;

    private ManagedChannel channel;
    private ApprovalServiceGrpc.ApprovalServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        String host = serverAddress.replace("static://", "").split(":")[0];
        int port = Integer.parseInt(serverAddress.replace("static://", "").split(":")[1]);

        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = ApprovalServiceGrpc.newBlockingStub(channel);
        log.info("gRPC Client 초기화 완료: {}:{}", host, port);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * 결재 요청을 Approval Processing Service로 전달
     */
    public String requestApproval(ApprovalRequestDocument document) {
        try {
            ApprovalRequest.Builder requestBuilder = ApprovalRequest.newBuilder()
                    .setRequestId(document.getRequestId().intValue())
                    .setRequesterId(document.getRequesterId().intValue())
                    .setTitle(document.getTitle())
                    .setContent(document.getContent());

            for (ApprovalStep step : document.getSteps()) {
                Step grpcStep = Step.newBuilder()
                        .setStep(step.getStep())
                        .setApproverId(step.getApproverId().intValue())
                        .setStatus(step.getStatus())
                        .build();
                requestBuilder.addSteps(grpcStep);
            }

            ApprovalResponse response = blockingStub.requestApproval(requestBuilder.build());
            log.info("gRPC RequestApproval 응답: status={}", response.getStatus());
            return response.getStatus();
        } catch (Exception e) {
            log.error("gRPC RequestApproval 호출 실패: requestId={}", document.getRequestId(), e);
            return "error";
        }
    }
}
