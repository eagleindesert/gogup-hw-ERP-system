package com.example.demo.controller;

import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 알림 REST API 컨트롤러
 * Approval Request Service에서 호출하여 알림 전송
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 결재 결과 알림 전송
     * POST /notifications/approval
     * 
     * Request Body:
     * {
     *   "requestId": 1,
     *   "employeeId": 1,
     *   "result": "approved",
     *   "finalResult": "approved"
     * }
     * 
     * 또는 반려 시:
     * {
     *   "requestId": 1,
     *   "employeeId": 1,
     *   "result": "rejected",
     *   "rejectedBy": 7,
     *   "finalResult": "rejected"
     * }
     */
    @PostMapping("/approval")
    public ResponseEntity<NotificationResponse> sendApprovalNotification(
            @RequestBody NotificationRequest request) {
        
        log.info("알림 전송 API 호출: {}", request);

        boolean sent = notificationService.sendApprovalNotification(request);

        NotificationResponse response = NotificationResponse.builder()
                .success(sent)
                .employeeId(request.getEmployeeId())
                .message(sent ? "알림이 성공적으로 전송되었습니다." : "대상 사용자가 연결되어 있지 않습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 직원 연결 상태 확인
     * GET /notifications/status/{employeeId}
     */
    @GetMapping("/status/{employeeId}")
    public ResponseEntity<Map<String, Object>> checkConnectionStatus(
            @PathVariable Long employeeId) {
        
        boolean connected = notificationService.isEmployeeConnected(employeeId);
        
        return ResponseEntity.ok(Map.of(
                "employeeId", employeeId,
                "connected", connected
        ));
    }

    /**
     * 전체 활성 세션 정보 조회
     * GET /notifications/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessionInfo() {
        int activeCount = notificationService.getActiveSessionCount();
        
        return ResponseEntity.ok(Map.of(
                "activeSessions", activeCount
        ));
    }
}
