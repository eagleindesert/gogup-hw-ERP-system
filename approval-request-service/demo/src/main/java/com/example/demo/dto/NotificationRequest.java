package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private Long requestId;
    private Long employeeId;
    private String result;
    private Long rejectedBy;
    private Long approvedBy;       // 승인한 결재자 ID
    private Integer currentStep;   // 현재 완료된 단계
    private Integer totalSteps;    // 전체 단계 수
    private String finalResult;
}
