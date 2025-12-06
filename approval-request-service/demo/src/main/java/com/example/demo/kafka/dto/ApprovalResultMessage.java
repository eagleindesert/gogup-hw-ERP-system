package com.example.demo.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka 메시지 - 결재 결과 (Processing Service → Request Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResultMessage {
    
    private Long requestId;
    private int step;
    private Long approverId;
    private String status;  // approved or rejected
}
