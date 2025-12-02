package com.example.demo.controller;

import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.document.ApprovalStep;
import com.example.demo.dto.ApprovalCreateRequest;
import com.example.demo.dto.ApprovalResponse;
import com.example.demo.dto.StepRequest;
import com.example.demo.dto.StepResponse;
import com.example.demo.exception.ApprovalNotFoundException;
import com.example.demo.exception.InvalidStepOrderException;
import com.example.demo.service.ApprovalRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalRequestService approvalRequestService;

    @Test
    @DisplayName("결재 요청 생성 - 성공")
    void createApproval_Success() throws Exception {
        // Given
        ApprovalCreateRequest request = ApprovalCreateRequest.builder()
                .requesterId(1L)
                .title("휴가 신청")
                .content("연차 휴가 신청합니다.")
                .steps(Arrays.asList(
                        StepRequest.builder().step(1).approverId(2L).build(),
                        StepRequest.builder().step(2).approverId(3L).build()
                ))
                .build();

        when(approvalRequestService.createApproval(any(ApprovalCreateRequest.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(1));
    }

    @Test
    @DisplayName("결재 요청 생성 - 필수 필드 누락 시 400")
    void createApproval_MissingRequiredField_Returns400() throws Exception {
        // Given - requesterId 누락
        String invalidRequest = """
                {
                    "title": "휴가 신청",
                    "content": "연차 휴가 신청합니다.",
                    "steps": [
                        {"step": 1, "approverId": 2}
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결재 요청 생성 - 빈 steps 시 400")
    void createApproval_EmptySteps_Returns400() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "requesterId": 1,
                    "title": "휴가 신청",
                    "content": "연차 휴가 신청합니다.",
                    "steps": []
                }
                """;

        // When & Then
        mockMvc.perform(post("/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결재 요청 목록 조회 - 성공")
    void getAllApprovals_Success() throws Exception {
        // Given
        List<ApprovalResponse> responses = Arrays.asList(
                createMockApprovalResponse(1L, "테스트 결재 1"),
                createMockApprovalResponse(2L, "테스트 결재 2")
        );
        when(approvalRequestService.getAllApprovals()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("결재 요청 목록 조회 - 빈 목록")
    void getAllApprovals_EmptyList() throws Exception {
        // Given
        when(approvalRequestService.getAllApprovals()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("결재 요청 상세 조회 - 성공")
    void getApproval_Success() throws Exception {
        // Given
        ApprovalResponse response = createMockApprovalResponse(1L, "테스트 결재");
        when(approvalRequestService.getApproval(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/approvals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.title").value("테스트 결재"))
                .andExpect(jsonPath("$.finalStatus").value("in_progress"));
    }

    @Test
    @DisplayName("결재 요청 상세 조회 - 존재하지 않는 ID")
    void getApproval_NotFound() throws Exception {
        // Given
        when(approvalRequestService.getApproval(999L)).thenThrow(new ApprovalNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/approvals/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("결재 요청 생성 - 잘못된 단계 순서")
    void createApproval_InvalidStepOrder_Returns400() throws Exception {
        // Given - step이 1, 3으로 연속되지 않음
        String invalidRequest = """
                {
                    "requesterId": 1,
                    "title": "휴가 신청",
                    "content": "연차 휴가 신청합니다.",
                    "steps": [
                        {"step": 1, "approverId": 2},
                        {"step": 3, "approverId": 3}
                    ]
                }
                """;

        when(approvalRequestService.createApproval(any(ApprovalCreateRequest.class)))
                .thenThrow(new InvalidStepOrderException());

        // When & Then
        mockMvc.perform(post("/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    private ApprovalResponse createMockApprovalResponse(Long requestId, String title) {
        List<StepResponse> steps = Arrays.asList(
                StepResponse.builder()
                        .step(1)
                        .approverId(2L)
                        .status("pending")
                        .build()
        );

        return ApprovalResponse.builder()
                .requestId(requestId)
                .requesterId(1L)
                .title(title)
                .content("테스트 내용")
                .steps(steps)
                .finalStatus("in_progress")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
