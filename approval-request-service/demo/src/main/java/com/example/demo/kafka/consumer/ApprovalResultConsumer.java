package com.example.demo.kafka.consumer;

import com.example.demo.kafka.config.KafkaConfig;
import com.example.demo.kafka.dto.ApprovalResultMessage;
import com.example.demo.service.ApprovalRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 결재 결과를 Kafka에서 수신하는 Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalResultConsumer {

    private final ApprovalRequestService approvalRequestService;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_APPROVAL_RESULT,
            groupId = "approval-request-service",
            containerFactory = "approvalResultListenerContainerFactory"
    )
    public void consumeApprovalResult(ApprovalResultMessage message) {
        log.info("Kafka 메시지 수신: topic={}, requestId={}, step={}, status={}",
                KafkaConfig.TOPIC_APPROVAL_RESULT,
                message.getRequestId(),
                message.getStep(),
                message.getStatus());

        try {
            approvalRequestService.processApprovalResult(
                    message.getRequestId(),
                    message.getStep(),
                    message.getApproverId(),
                    message.getStatus()
            );
            log.info("결재 결과 처리 완료: requestId={}", message.getRequestId());
        } catch (Exception e) {
            log.error("결재 결과 처리 실패: requestId={}", message.getRequestId(), e);
        }
    }
}
