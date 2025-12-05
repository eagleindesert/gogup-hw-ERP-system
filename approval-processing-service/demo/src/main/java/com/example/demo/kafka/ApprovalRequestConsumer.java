package com.example.demo.kafka;

import com.example.demo.model.PendingApproval;
import com.example.demo.repository.InMemoryApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kafka Consumer - 결재 요청 수신
 * Approval Request Service에서 발행한 결재 요청을 소비하여 In-Memory에 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalRequestConsumer {

    private final InMemoryApprovalRepository repository;

    @KafkaListener(topics = "approval-request", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeApprovalRequest(ApprovalRequestMessage message) {
        log.info("Kafka 결재 요청 수신: requestId={}, title={}", 
                message.getRequestId(), message.getTitle());

        try {
            // ApprovalRequestMessage를 PendingApproval로 변환
            PendingApproval pendingApproval = convertToPendingApproval(message);

            // 첫 번째 pending 상태의 결재자 찾기
            Optional<ApprovalRequestMessage.StepInfo> pendingStep = message.getSteps().stream()
                    .filter(s -> "pending".equals(s.getStatus()))
                    .findFirst();

            if (pendingStep.isPresent()) {
                Long approverId = pendingStep.get().getApproverId();
                // In-Memory Repository에 해당 결재자의 대기 목록에 추가
                repository.addPendingApproval(approverId, pendingApproval);
                
                log.info("결재 요청 저장 완료: requestId={}, approverId={}", 
                        message.getRequestId(), approverId);
            } else {
                log.warn("pending 상태의 결재 단계가 없습니다: requestId={}", message.getRequestId());
            }
        } catch (Exception e) {
            log.error("결재 요청 처리 실패: requestId={}, error={}", 
                    message.getRequestId(), e.getMessage(), e);
            // 실패 시 별도 처리 (Dead Letter Queue 등) 가능
        }
    }

    private PendingApproval convertToPendingApproval(ApprovalRequestMessage message) {
        return PendingApproval.builder()
                .requestId(message.getRequestId())
                .requesterId(message.getRequesterId())
                .title(message.getTitle())
                .content(message.getContent())
                .steps(message.getSteps().stream()
                        .map(step -> PendingApproval.StepInfo.builder()
                                .step(step.getStep())
                                .approverId(step.getApproverId())
                                .status(step.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
