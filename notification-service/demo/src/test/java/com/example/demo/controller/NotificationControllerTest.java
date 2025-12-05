package com.example.demo.controller;

import com.example.demo.dto.NotificationRequest;
import com.example.demo.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController 테스트")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("POST /notifications/approval - 승인 알림 전송 성공")
    void sendApprovalNotification_approved() throws Exception {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .requestId(1L)
                .employeeId(100L)
                .result("approved")
                .finalResult("approved")
                .build();

        when(notificationService.sendApprovalNotification(any())).thenReturn(true);

        // when & then
        mockMvc.perform(post("/notifications/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.message").value("알림이 성공적으로 전송되었습니다."));
    }

    @Test
    @DisplayName("POST /notifications/approval - 반려 알림 전송 성공")
    void sendApprovalNotification_rejected() throws Exception {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .requestId(2L)
                .employeeId(100L)
                .result("rejected")
                .rejectedBy(50L)
                .finalResult("rejected")
                .build();

        when(notificationService.sendApprovalNotification(any())).thenReturn(true);

        // when & then
        mockMvc.perform(post("/notifications/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /notifications/approval - 전송 실패 (세션 없음)")
    void sendApprovalNotification_noSession() throws Exception {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .requestId(1L)
                .employeeId(100L)
                .result("approved")
                .finalResult("approved")
                .build();

        when(notificationService.sendApprovalNotification(any())).thenReturn(false);

        // when & then
        mockMvc.perform(post("/notifications/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("대상 사용자가 연결되어 있지 않습니다."));
    }

    @Test
    @DisplayName("GET /notifications/status/{employeeId} - 연결 상태 확인")
    void checkConnectionStatus() throws Exception {
        // given
        when(notificationService.isEmployeeConnected(100L)).thenReturn(true);

        // when & then
        mockMvc.perform(get("/notifications/status/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(100))
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    @DisplayName("GET /notifications/sessions - 세션 정보 조회")
    void getSessionInfo() throws Exception {
        // given
        when(notificationService.getActiveSessionCount()).thenReturn(3);

        // when & then
        mockMvc.perform(get("/notifications/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeSessions").value(3));
    }
}
