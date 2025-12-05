package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.dto.ApprovalCreateRequest;
import com.example.demo.dto.ApprovalIdResponse;
import com.example.demo.dto.ApprovalResponse;
import com.example.demo.service.ApprovalRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalRequestService approvalRequestService;

    /**
     * 결재 요청 생성
     * POST /approvals
     */
    @PostMapping
    public ResponseEntity<ApprovalIdResponse> createApproval(
            @Valid @RequestBody ApprovalCreateRequest request) {
        Long requestId = approvalRequestService.createApproval(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApprovalIdResponse(requestId));
    }

    /**
     * 결재 요청 목록 조회
     * GET /approvals
     */
    @GetMapping
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        List<ApprovalResponse> approvals = approvalRequestService.getAllApprovals();
        if (approvals == null || approvals.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(approvals);
    }

    /**
     * pending 상태인 결재 요청 목록 조회
     * GET /approvals/pending
     * Approval Processing Service에서 서버 시작 시 동기화용으로 호출
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalRequestDocument>> getPendingApprovals() {
        List<ApprovalRequestDocument> pendingApprovals = approvalRequestService.getAllPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }

    /**
     * 결재 요청 상세 조회
     * GET /approvals/{requestId}
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApprovalResponse> getApproval(@PathVariable Long requestId) {
        ApprovalResponse approval = approvalRequestService.getApproval(requestId);
        return ResponseEntity.ok(approval);
    }
}
