package com.example.demo.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kafka 메시지 - 결재 요청 (Request Service → Processing Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestMessage {
    
    private Long requestId;
    private Long requesterId;
    private String title;
    private String content;
    private List<StepInfo> steps;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepInfo {
        private int step;
        private Long approverId;
        private String status;
    }
}
