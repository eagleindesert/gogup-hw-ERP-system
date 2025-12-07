package com.example.demo.document;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStep {

    private Integer step;
    private Long approverId;
    private String status; // pending, approved, rejected
    private String comment; // 결재 의견/코멘트
    private LocalDateTime updatedAt;
}
