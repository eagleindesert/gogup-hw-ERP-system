package com.example.demo.controller;

import com.example.demo.dto.ApprovalCreateRequest;
import com.example.demo.dto.ApprovalIdResponse;
import com.example.demo.dto.ApprovalResponse;
import com.example.demo.service.ApprovalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ResponseEntity.ok(approvals);
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
