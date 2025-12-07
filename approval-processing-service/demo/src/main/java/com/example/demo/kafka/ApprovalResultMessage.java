package com.example.demo.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka를 통해 전송하는 결재 결과 메시지 DTO
 * approval-result 토픽으로 발행
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResultMessage {
    private Long requestId;
    private int step;
    private Long approverId;
    private String status; // approved 또는 rejected
    private String comment; // 결재 의견/코멘트
}
