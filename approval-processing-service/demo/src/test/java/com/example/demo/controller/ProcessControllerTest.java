package com.example.demo.controller;

import com.example.demo.dto.ProcessRequest;
import com.example.demo.dto.ProcessResponse;
import com.example.demo.exception.ApprovalNotFoundException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.InvalidStatusException;
import com.example.demo.model.PendingApproval;
import com.example.demo.service.ApprovalProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ProcessController 테스트")
@WebMvcTest(ProcessController.class)
@Import(GlobalExceptionHandler.class)
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalProcessingService service;

    @Test
    @DisplayName("결재 대기 목록 조회 - 성공")
    void getPendingApprovals_success() throws Exception {
        // given
        Long approverId = 1L;
        PendingApproval approval = PendingApproval.builder()
                .requestId(100L)
                .requesterId(10L)
                .title("휴가 신청")
                .content("연차 휴가 신청합니다.")
                .steps(List.of(
                        PendingApproval.StepInfo.builder()
                                .step(1)
                                .approverId(1L)
                                .status("pending")
                                .build()
                ))
                .build();

        given(service.getPendingApprovals(approverId)).willReturn(List.of(approval));

        // when & then
        mockMvc.perform(get("/process/{approverId}", approverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value(100))
                .andExpect(jsonPath("$[0].title").value("휴가 신청"));
    }

    @Test
    @DisplayName("결재 대기 목록 조회 - 빈 목록")
    void getPendingApprovals_empty() throws Exception {
        // given
        Long approverId = 1L;
        given(service.getPendingApprovals(approverId)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/process/{approverId}", approverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("결재 처리 - 승인 성공")
    void processApproval_approve_success() throws Exception {
        // given
        Long approverId = 1L;
        Long requestId = 100L;
        ProcessRequest request = ProcessRequest.builder().status("approved").build();
        ProcessResponse response = ProcessResponse.builder()
                .requestId(requestId)
                .approverId(approverId)
                .status("approved")
                .message("결재 처리가 완료되었습니다.")
                .build();

        given(service.processApproval(eq(approverId), eq(requestId), eq("approved")))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/process/{approverId}/{requestId}", approverId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(100))
                .andExpect(jsonPath("$.status").value("approved"))
                .andExpect(jsonPath("$.message").value("결재 처리가 완료되었습니다."));
    }

    @Test
    @DisplayName("결재 처리 - 반려 성공")
    void processApproval_reject_success() throws Exception {
        // given
        Long approverId = 1L;
        Long requestId = 100L;
        ProcessRequest request = ProcessRequest.builder().status("rejected").build();
        ProcessResponse response = ProcessResponse.builder()
                .requestId(requestId)
                .approverId(approverId)
                .status("rejected")
                .message("결재 처리가 완료되었습니다.")
                .build();

        given(service.processApproval(eq(approverId), eq(requestId), eq("rejected")))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/process/{approverId}/{requestId}", approverId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("rejected"));
    }

    @Test
    @DisplayName("결재 처리 - 유효하지 않은 상태값")
    void processApproval_invalidStatus() throws Exception {
        // given
        Long approverId = 1L;
        Long requestId = 100L;
        ProcessRequest request = ProcessRequest.builder().status("invalid").build();

        given(service.processApproval(eq(approverId), eq(requestId), eq("invalid")))
                .willThrow(new InvalidStatusException("유효하지 않은 상태값입니다: invalid"));

        // when & then
        mockMvc.perform(post("/process/{approverId}/{requestId}", approverId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 상태값입니다: invalid"));
    }

    @Test
    @DisplayName("결재 처리 - 결재 요청 없음")
    void processApproval_notFound() throws Exception {
        // given
        Long approverId = 1L;
        Long requestId = 999L;
        ProcessRequest request = ProcessRequest.builder().status("approved").build();

        given(service.processApproval(eq(approverId), eq(requestId), eq("approved")))
                .willThrow(new ApprovalNotFoundException("결재 요청을 찾을 수 없습니다"));

        // when & then
        mockMvc.perform(post("/process/{approverId}/{requestId}", approverId, requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Approval Not Found"));
    }
}
