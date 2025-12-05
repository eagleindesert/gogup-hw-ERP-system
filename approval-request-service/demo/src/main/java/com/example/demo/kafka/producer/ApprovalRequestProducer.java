package com.example.demo.kafka.producer;

import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.kafka.config.KafkaConfig;
import com.example.demo.kafka.dto.ApprovalRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 결재 요청을 Kafka로 전송하는 Producer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalRequestProducer {

    private final KafkaTemplate<String, ApprovalRequestMessage> kafkaTemplate;

    /**
     * 결재 요청을 Processing Service로 전송
     */
    public void sendApprovalRequest(ApprovalRequestDocument document) {
        ApprovalRequestMessage message = convertToMessage(document);
        
        String key = String.valueOf(document.getRequestId());
        
        CompletableFuture<SendResult<String, ApprovalRequestMessage>> future = 
                kafkaTemplate.send(KafkaConfig.TOPIC_APPROVAL_REQUEST, key, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Kafka 메시지 전송 성공: topic={}, requestId={}, offset={}",
                        KafkaConfig.TOPIC_APPROVAL_REQUEST,
                        message.getRequestId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Kafka 메시지 전송 실패: requestId={}", message.getRequestId(), ex);
            }
        });
    }

    private ApprovalRequestMessage convertToMessage(ApprovalRequestDocument document) {
        return ApprovalRequestMessage.builder()
                .requestId(document.getRequestId())
                .requesterId(document.getRequesterId())
                .title(document.getTitle())
                .content(document.getContent())
                .steps(document.getSteps().stream()
                        .map(step -> ApprovalRequestMessage.StepInfo.builder()
                                .step(step.getStep())
                                .approverId(step.getApproverId())
                                .status(step.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
