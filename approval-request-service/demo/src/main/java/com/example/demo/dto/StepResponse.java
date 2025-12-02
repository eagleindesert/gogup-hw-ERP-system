package com.example.demo.dto;

import com.example.demo.document.ApprovalStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepResponse {

    private Integer step;
    private Long approverId;
    private String status;
    private LocalDateTime updatedAt;

    public static StepResponse from(ApprovalStep step) {
        return StepResponse.builder()
                .step(step.getStep())
                .approverId(step.getApproverId())
                .status(step.getStatus())
                .updatedAt(step.getUpdatedAt())
                .build();
    }
}
