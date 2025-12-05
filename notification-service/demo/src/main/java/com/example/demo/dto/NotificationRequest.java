package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 알림 요청 DTO (Approval Request Service → Notification Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    private Long requestId;       // 결재 요청 ID
    private Long employeeId;      // 알림 받을 직원 ID (요청자)
    private String result;        // 결과 (approved, rejected)
    private Long rejectedBy;      // 반려한 결재자 ID (반려 시에만)
    private Long approvedBy;      // 승인한 결재자 ID (중간 승인 시)
    private Integer currentStep;  // 현재 완료된 단계
    private Integer totalSteps;   // 전체 단계 수
    private String finalResult;   // 최종 결과 (approved, rejected, in_progress)
}
