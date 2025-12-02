package com.example.demo.dto;

import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.document.ApprovalStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponse {

    private Long requestId;
    private Long requesterId;
    private String title;
    private String content;
    private List<StepResponse> steps;
    private String finalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApprovalResponse from(ApprovalRequestDocument doc) {
        return ApprovalResponse.builder()
                .requestId(doc.getRequestId())
                .requesterId(doc.getRequesterId())
                .title(doc.getTitle())
                .content(doc.getContent())
                .steps(doc.getSteps().stream()
                        .map(StepResponse::from)
                        .toList())
                .finalStatus(doc.getFinalStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
