package com.example.demo.service;

import com.example.demo.dto.NotificationMessage;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 서비스 - WebSocket을 통한 실시간 알림 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationWebSocketHandler webSocketHandler;

    /**
     * 결재 결과 알림 전송
     */
    public boolean sendApprovalNotification(NotificationRequest request) {
        log.info("결재 알림 전송 요청: requestId={}, employeeId={}, result={}", 
                request.getRequestId(), request.getEmployeeId(), request.getFinalResult());

        // 알림 메시지 생성
        NotificationMessage message = buildNotificationMessage(request);

        // WebSocket으로 전송
        boolean sent = webSocketHandler.sendMessage(request.getEmployeeId(), message);

        if (sent) {
            log.info("알림 전송 성공: employeeId={}", request.getEmployeeId());
        } else {
            log.warn("알림 전송 실패 (세션 없음): employeeId={}", request.getEmployeeId());
        }

        return sent;
    }

    /**
     * NotificationRequest를 WebSocket 메시지로 변환
     */
    private NotificationMessage buildNotificationMessage(NotificationRequest request) {
        String userMessage;
        
        if ("approved".equals(request.getFinalResult())) {
            userMessage = String.format("결재 요청 #%d이(가) 최종 승인되었습니다.", request.getRequestId());
        } else if ("rejected".equals(request.getFinalResult())) {
            userMessage = String.format("결재 요청 #%d이(가) 결재자 %d에 의해 반려되었습니다.", 
                    request.getRequestId(), request.getRejectedBy());
        } else if ("in_progress".equals(request.getFinalResult())) {
            // 중간 단계 승인
            userMessage = String.format("결재 요청 #%d: 결재자 %d가 승인했습니다. (%d/%d 단계 완료)", 
                    request.getRequestId(), 
                    request.getApprovedBy(),
                    request.getCurrentStep(),
                    request.getTotalSteps());
        } else {
            userMessage = String.format("결재 요청 #%d 상태 업데이트: %s", 
                    request.getRequestId(), request.getResult());
        }

        return NotificationMessage.builder()
                .type("APPROVAL_RESULT")
                .requestId(request.getRequestId())
                .result(request.getResult())
                .rejectedBy(request.getRejectedBy())
                .approvedBy(request.getApprovedBy())
                .currentStep(request.getCurrentStep())
                .totalSteps(request.getTotalSteps())
                .finalResult(request.getFinalResult())
                .message(userMessage)
                .build();
    }

    /**
     * 직원 연결 상태 확인
     */
    public boolean isEmployeeConnected(Long employeeId) {
        return webSocketHandler.isConnected(employeeId);
    }

    /**
     * 활성 세션 수 조회
     */
    public int getActiveSessionCount() {
        return webSocketHandler.getActiveSessionCount();
    }
}
