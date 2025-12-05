package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 알림 전송 결과 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private boolean success;      // 전송 성공 여부
    private String message;       // 결과 메시지
    private Long employeeId;      // 대상 직원 ID
}
