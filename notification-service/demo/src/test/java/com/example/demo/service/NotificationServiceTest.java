package com.example.demo.service;

import com.example.demo.dto.NotificationRequest;
import com.example.demo.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest approvedRequest;
    private NotificationRequest rejectedRequest;

    @BeforeEach
    void setUp() {
        approvedRequest = NotificationRequest.builder()
                .requestId(1L)
                .employeeId(100L)
                .result("approved")
                .finalResult("approved")
                .build();

        rejectedRequest = NotificationRequest.builder()
                .requestId(2L)
                .employeeId(100L)
                .result("rejected")
                .rejectedBy(50L)
                .finalResult("rejected")
                .build();
    }

    @Test
    @DisplayName("승인 알림 전송 성공")
    void sendApprovalNotification_approved_success() {
        // given
        when(webSocketHandler.sendMessage(eq(100L), any())).thenReturn(true);

        // when
        boolean result = notificationService.sendApprovalNotification(approvedRequest);

        // then
        assertTrue(result);
        verify(webSocketHandler, times(1)).sendMessage(eq(100L), any());
    }

    @Test
    @DisplayName("반려 알림 전송 성공")
    void sendApprovalNotification_rejected_success() {
        // given
        when(webSocketHandler.sendMessage(eq(100L), any())).thenReturn(true);

        // when
        boolean result = notificationService.sendApprovalNotification(rejectedRequest);

        // then
        assertTrue(result);
        verify(webSocketHandler, times(1)).sendMessage(eq(100L), any());
    }

    @Test
    @DisplayName("알림 전송 실패 - 세션 없음")
    void sendApprovalNotification_noSession() {
        // given
        when(webSocketHandler.sendMessage(eq(100L), any())).thenReturn(false);

        // when
        boolean result = notificationService.sendApprovalNotification(approvedRequest);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("직원 연결 상태 확인 - 연결됨")
    void isEmployeeConnected_true() {
        // given
        when(webSocketHandler.isConnected(100L)).thenReturn(true);

        // when
        boolean result = notificationService.isEmployeeConnected(100L);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("직원 연결 상태 확인 - 연결 안됨")
    void isEmployeeConnected_false() {
        // given
        when(webSocketHandler.isConnected(100L)).thenReturn(false);

        // when
        boolean result = notificationService.isEmployeeConnected(100L);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("활성 세션 수 조회")
    void getActiveSessionCount() {
        // given
        when(webSocketHandler.getActiveSessionCount()).thenReturn(5);

        // when
        int count = notificationService.getActiveSessionCount();

        // then
        assertEquals(5, count);
    }
}
