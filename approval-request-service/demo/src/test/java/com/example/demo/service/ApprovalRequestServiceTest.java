package com.example.demo.service;

import com.example.demo.client.EmployeeServiceClient;
import com.example.demo.client.NotificationServiceClient;
import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.document.ApprovalStep;
import com.example.demo.dto.ApprovalCreateRequest;
import com.example.demo.dto.ApprovalResponse;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.StepRequest;
import com.example.demo.exception.ApprovalNotFoundException;
import com.example.demo.exception.InvalidStepOrderException;
import com.example.demo.grpc.ApprovalProcessingGrpcClient;
import com.example.demo.repository.ApprovalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalRequestServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private EmployeeServiceClient employeeServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private ApprovalProcessingGrpcClient approvalProcessingGrpcClient;

    @InjectMocks
    private ApprovalRequestService approvalRequestService;

    private ApprovalCreateRequest createRequest;
    private ApprovalRequestDocument savedDocument;

    @BeforeEach
    void setUp() {
        createRequest = ApprovalCreateRequest.builder()
                .requesterId(1L)
                .title("휴가 신청")
                .content("연차 휴가 신청합니다.")
                .steps(Arrays.asList(
                        StepRequest.builder().step(1).approverId(2L).build(),
                        StepRequest.builder().step(2).approverId(3L).build()
                ))
                .build();

        savedDocument = ApprovalRequestDocument.builder()
                .id("test-id")
                .requestId(1L)
                .requesterId(1L)
                .title("휴가 신청")
                .content("연차 휴가 신청합니다.")
                .steps(Arrays.asList(
                        ApprovalStep.builder().step(1).approverId(2L).status("pending").build(),
                        ApprovalStep.builder().step(2).approverId(3L).status("pending").build()
                ))
                .finalStatus("in_progress")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("결재 요청 생성 - 성공")
    void createApproval_Success() {
        // Given
        when(approvalRequestRepository.findTopByOrderByRequestIdDesc()).thenReturn(Optional.empty());
        when(approvalRequestRepository.save(any(ApprovalRequestDocument.class))).thenReturn(savedDocument);
        when(approvalProcessingGrpcClient.requestApproval(any())).thenReturn("SUCCESS");

        // When
        Long requestId = approvalRequestService.createApproval(createRequest);

        // Then
        assertThat(requestId).isEqualTo(1L);
        verify(employeeServiceClient).validateEmployee(1L);
        verify(employeeServiceClient).validateEmployee(2L);
        verify(employeeServiceClient).validateEmployee(3L);
        verify(approvalRequestRepository).save(any(ApprovalRequestDocument.class));
        verify(approvalProcessingGrpcClient).requestApproval(any());
    }

    @Test
    @DisplayName("결재 요청 생성 - 잘못된 단계 순서")
    void createApproval_InvalidStepOrder_ThrowsException() {
        // Given
        ApprovalCreateRequest invalidRequest = ApprovalCreateRequest.builder()
                .requesterId(1L)
                .title("테스트")
                .content("테스트 내용")
                .steps(Arrays.asList(
                        StepRequest.builder().step(1).approverId(2L).build(),
                        StepRequest.builder().step(3).approverId(3L).build() // 2가 아닌 3
                ))
                .build();

        // When & Then
        assertThatThrownBy(() -> approvalRequestService.createApproval(invalidRequest))
                .isInstanceOf(InvalidStepOrderException.class);
    }

    @Test
    @DisplayName("결재 요청 목록 조회 - 성공")
    void getAllApprovals_Success() {
        // Given
        List<ApprovalRequestDocument> documents = Arrays.asList(savedDocument);
        when(approvalRequestRepository.findAll()).thenReturn(documents);

        // When
        List<ApprovalResponse> result = approvalRequestService.getAllApprovals();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequestId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("결재 요청 상세 조회 - 성공")
    void getApproval_Success() {
        // Given
        when(approvalRequestRepository.findByRequestId(1L)).thenReturn(Optional.of(savedDocument));

        // When
        ApprovalResponse result = approvalRequestService.getApproval(1L);

        // Then
        assertThat(result.getRequestId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("휴가 신청");
    }

    @Test
    @DisplayName("결재 요청 상세 조회 - 존재하지 않는 ID")
    void getApproval_NotFound_ThrowsException() {
        // Given
        when(approvalRequestRepository.findByRequestId(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalRequestService.getApproval(999L))
                .isInstanceOf(ApprovalNotFoundException.class);
    }

    @Test
    @DisplayName("결재 결과 처리 - 승인 (다음 단계 존재)")
    void processApprovalResult_Approved_NextStepExists() {
        // Given
        when(approvalRequestRepository.findByRequestId(1L)).thenReturn(Optional.of(savedDocument));
        when(approvalRequestRepository.save(any())).thenReturn(savedDocument);
        when(approvalProcessingGrpcClient.requestApproval(any())).thenReturn("SUCCESS");

        // When
        approvalRequestService.processApprovalResult(1L, 1, 2L, "approved");

        // Then
        verify(approvalRequestRepository).save(any());
        verify(approvalProcessingGrpcClient).requestApproval(any());
        verify(notificationServiceClient, never()).sendNotification(any());
    }

    @Test
    @DisplayName("결재 결과 처리 - 최종 승인")
    void processApprovalResult_FinalApproval() {
        // Given
        ApprovalRequestDocument document = ApprovalRequestDocument.builder()
                .id("test-id")
                .requestId(1L)
                .requesterId(1L)
                .title("휴가 신청")
                .content("연차 휴가 신청합니다.")
                .steps(Arrays.asList(
                        ApprovalStep.builder().step(1).approverId(2L).status("approved").build(),
                        ApprovalStep.builder().step(2).approverId(3L).status("pending").build()
                ))
                .finalStatus("in_progress")
                .createdAt(LocalDateTime.now())
                .build();

        when(approvalRequestRepository.findByRequestId(1L)).thenReturn(Optional.of(document));
        when(approvalRequestRepository.save(any())).thenReturn(document);

        // When
        approvalRequestService.processApprovalResult(1L, 2, 3L, "approved");

        // Then
        ArgumentCaptor<ApprovalRequestDocument> captor = ArgumentCaptor.forClass(ApprovalRequestDocument.class);
        verify(approvalRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getFinalStatus()).isEqualTo("approved");
        verify(notificationServiceClient).sendNotification(any());
    }

    @Test
    @DisplayName("결재 결과 처리 - 반려")
    void processApprovalResult_Rejected() {
        // Given
        when(approvalRequestRepository.findByRequestId(1L)).thenReturn(Optional.of(savedDocument));
        when(approvalRequestRepository.save(any())).thenReturn(savedDocument);

        // When
        approvalRequestService.processApprovalResult(1L, 1, 2L, "rejected");

        // Then
        ArgumentCaptor<ApprovalRequestDocument> captor = ArgumentCaptor.forClass(ApprovalRequestDocument.class);
        verify(approvalRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getFinalStatus()).isEqualTo("rejected");

        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationServiceClient).sendNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getResult()).isEqualTo("rejected");
    }
}
