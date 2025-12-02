package com.example.demo.controller;

import com.example.demo.dto.ProcessRequest;
import com.example.demo.dto.ProcessResponse;
import com.example.demo.model.PendingApproval;
import com.example.demo.service.ApprovalProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결재 처리 REST 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ApprovalProcessingService service;

    /**
     * 특정 결재자의 대기 중인 결재 목록 조회
     * GET /process/{approverId}
     */
    @GetMapping("/{approverId}")
    public ResponseEntity<List<PendingApproval>> getPendingApprovals(@PathVariable Long approverId) {
        log.info("결재 대기 목록 조회 요청: approverId={}", approverId);
        List<PendingApproval> pendingApprovals = service.getPendingApprovals(approverId);
        return ResponseEntity.ok(pendingApprovals);
    }

    /**
     * 결재 처리 (승인/반려)
     * POST /process/{approverId}/{requestId}
     */
    @PostMapping("/{approverId}/{requestId}")
    public ResponseEntity<ProcessResponse> processApproval(
            @PathVariable Long approverId,
            @PathVariable Long requestId,
            @RequestBody ProcessRequest request) {
        log.info("결재 처리 요청: approverId={}, requestId={}, status={}", approverId, requestId, request.getStatus());
        ProcessResponse response = service.processApproval(approverId, requestId, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
