package com.example.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer - 결재 결과 전송
 * 결재 처리 결과를 Approval Request Service로 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalResultProducer {

    private static final String TOPIC = "approval-result";
    private final KafkaTemplate<String, ApprovalResultMessage> kafkaTemplate;

    /**
     * 결재 결과를 Kafka로 전송
     */
    public void sendApprovalResult(Long requestId, int step, Long approverId, String status) {
        ApprovalResultMessage message = ApprovalResultMessage.builder()
                .requestId(requestId)
                .step(step)
                .approverId(approverId)
                .status(status)
                .build();

        String key = String.valueOf(requestId);
        
        CompletableFuture<SendResult<String, ApprovalResultMessage>> future = 
                kafkaTemplate.send(TOPIC, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("결재 결과 Kafka 전송 성공: requestId={}, step={}, status={}, partition={}, offset={}",
                        requestId, step, status,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("결재 결과 Kafka 전송 실패: requestId={}, error={}", 
                        requestId, ex.getMessage(), ex);
            }
        });
    }
}
