package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 결재 대기 정보를 담는 모델 클래스
 * In-Memory에 저장되는 결재 요청 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingApproval {

    private Long requestId;
    private Long requesterId;
    private String title;
    private String content;
    private List<StepInfo> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepInfo {
        private Integer step;
        private Long approverId;
        private String status;
    }
}
