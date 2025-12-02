package com.example.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStep {

    private Integer step;
    private Long approverId;
    private String status; // pending, approved, rejected
    private LocalDateTime updatedAt;
}
