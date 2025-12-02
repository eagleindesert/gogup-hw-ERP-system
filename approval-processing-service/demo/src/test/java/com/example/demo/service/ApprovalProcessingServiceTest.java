package com.example.demo.service;

import com.example.demo.dto.ProcessResponse;
import com.example.demo.exception.ApprovalNotFoundException;
import com.example.demo.exception.InvalidStatusException;
import com.example.demo.grpc.ApprovalResultGrpcClient;
import com.example.demo.model.PendingApproval;
import com.example.demo.repository.InMemoryApprovalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("ApprovalProcessingService 테스트")
@ExtendWith(MockitoExtension.class)
class ApprovalProcessingServiceTest {

    @Mock
    private InMemoryApprovalRepository repository;

    @Mock
    private ApprovalResultGrpcClient grpcClient;

    @InjectMocks
    private ApprovalProcessingService service;

    private PendingApproval testApproval;

    @BeforeEach
    void setUp() {
        testApproval = PendingApproval.builder()
                .requestId(100L)
                .requesterId(10L)
                .title("휴가 신청")
                .content("연차 휴가 신청합니다.")
                .steps(List.of(
                        PendingApproval.StepInfo.builder()
                                .step(1)
                                .approverId(1L)
                                .status("pending")
                                .build(),
                        PendingApproval.StepInfo.builder()
                                .step(2)
                                .approverId(2L)
                                .status("pending")
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("결재 대기 목록 조회 - 성공")
    void getPendingApprovals_success() {
        // given
        Long approverId = 1L;
        given(repository.getPendingApprovals(approverId))
                .willReturn(List.of(testApproval));

        // when
        List<PendingApproval> result = service.getPendingApprovals(approverId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequestId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("결재 대기 목록 조회 - 빈 목록")
    void getPendingApprovals_empty() {
        // given
        Long approverId = 1L;
        given(repository.getPendingApprovals(approverId))
                .willReturn(List.of());

        // when
        List<PendingApproval> result = service.getPendingApprovals(approverId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("결재 처리 - 승인 성공")
    void processApproval_approve_success() {
        // given
        Long approverId = 1L;
        Long requestId = 100L;
        String status = "approved";

        List<PendingApproval> pendingList = new ArrayList<>();
        pendingList.add(testApproval);

        given(repository.getPendingApprovals(approverId)).willReturn(pendingList);
        given(grpcClient.returnApprovalResult(eq(requestId), eq(1), eq(approverId), eq(status)))
                .willReturn("updated");

        // when
        ProcessResponse result = service.processApproval(approverId, requestId, status);

        // then
        assertThat(result.getRequestId()).isEqualTo(requestId);
        assertThat(result.getApproverId()).isEqualTo(approverId);
        assertThat(result.getStatus()).isEqualTo(status);
        verify(repository).removePendingApproval(approverId, requestId);
    }

    @Test
    @DisplayName("결재 처리 - 반려 성공")
    void processApproval_reject_success() {
        // given
        Long approverId = 1L;
        Long requestId = 100L;
        String status = "rejected";

        List<PendingApproval> pendingList = new ArrayList<>();
        pendingList.add(testApproval);

        given(repository.getPendingApprovals(approverId)).willReturn(pendingList);
        given(grpcClient.returnApprovalResult(eq(requestId), eq(1), eq(approverId), eq(status)))
                .willReturn("updated");

        // when
        ProcessResponse result = service.processApproval(approverId, requestId, status);

        // then
        assertThat(result.getStatus()).isEqualTo("rejected");
    }

    @Test
    @DisplayName("결재 처리 - 유효하지 않은 상태값")
    void processApproval_invalidStatus() {
        // given
        Long approverId = 1L;
        Long requestId = 100L;
        String status = "invalid";

        // when & then
        assertThatThrownBy(() -> service.processApproval(approverId, requestId, status))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("유효하지 않은 상태값");
    }

    @Test
    @DisplayName("결재 처리 - 결재 요청 없음")
    void processApproval_notFound() {
        // given
        Long approverId = 1L;
        Long requestId = 999L;
        String status = "approved";

        given(repository.getPendingApprovals(approverId)).willReturn(List.of(testApproval));

        // when & then
        assertThatThrownBy(() -> service.processApproval(approverId, requestId, status))
                .isInstanceOf(ApprovalNotFoundException.class)
                .hasMessageContaining("결재 요청을 찾을 수 없습니다");
    }
}
