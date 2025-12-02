package com.example.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "approval_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequestDocument {

    @Id
    private String id;

    private Long requestId;
    private Long requesterId;
    private String title;
    private String content;
    private List<ApprovalStep> steps;
    private String finalStatus; // in_progress, approved, rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
