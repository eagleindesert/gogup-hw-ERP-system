package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket으로 전송되는 알림 메시지 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값은 JSON에서 제외
public class NotificationMessage {
    
    private String type;          // 메시지 타입 (APPROVAL_RESULT)
    private Long requestId;       // 결재 요청 ID
    private String result;        // 결과 (approved, rejected)
    private Long rejectedBy;      // 반려한 결재자 ID (반려 시에만)
    private Long approvedBy;      // 승인한 결재자 ID (중간 승인 시)
    private Integer currentStep;  // 현재 완료된 단계
    private Integer totalSteps;   // 전체 단계 수
    private String finalResult;   // 최종 결과 (approved, rejected, in_progress)
    private String message;       // 사용자 친화적 메시지
}
