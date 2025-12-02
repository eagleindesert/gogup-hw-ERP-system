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
    private String finalResult;
}
