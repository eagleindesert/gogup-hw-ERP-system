package com.example.demo.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kafka를 통해 수신하는 결재 요청 메시지 DTO
 * approval-request 토픽에서 소비
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
