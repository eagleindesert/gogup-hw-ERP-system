package com.example.demo.repository;

import com.example.demo.model.PendingApproval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryApprovalRepository 테스트")
class InMemoryApprovalRepositoryTest {

    private InMemoryApprovalRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryApprovalRepository();
    }

    @Test
    @DisplayName("결재 대기 추가 - 새로운 결재자")
    void addPendingApproval_newApproverId() {
        // given
        Long approverId = 1L;
        PendingApproval approval = createPendingApproval(100L, 10L, "테스트 결재");

        // when
        repository.addPendingApproval(approverId, approval);

        // then
        List<PendingApproval> result = repository.getPendingApprovals(approverId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequestId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("결재 대기 추가 - 기존 결재자에 추가")
    void addPendingApproval_existingApproverId() {
        // given
        Long approverId = 1L;
        PendingApproval approval1 = createPendingApproval(100L, 10L, "테스트 결재 1");
        PendingApproval approval2 = createPendingApproval(101L, 11L, "테스트 결재 2");

        // when
        repository.addPendingApproval(approverId, approval1);
        repository.addPendingApproval(approverId, approval2);

        // then
        List<PendingApproval> result = repository.getPendingApprovals(approverId);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("결재 대기 목록 조회 - 빈 목록")
    void getPendingApprovals_empty() {
        // given
        Long approverId = 999L;

        // when
        List<PendingApproval> result = repository.getPendingApprovals(approverId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("결재 대기 제거 - 성공")
    void removePendingApproval_success() {
        // given
        Long approverId = 1L;
        PendingApproval approval = createPendingApproval(100L, 10L, "테스트 결재");
        repository.addPendingApproval(approverId, approval);

        // when
        repository.removePendingApproval(approverId, 100L);

        // then
        List<PendingApproval> result = repository.getPendingApprovals(approverId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("결재 대기 제거 - 존재하지 않는 요청")
    void removePendingApproval_notFound() {
        // given
        Long approverId = 1L;
        PendingApproval approval = createPendingApproval(100L, 10L, "테스트 결재");
        repository.addPendingApproval(approverId, approval);

        // when
        repository.removePendingApproval(approverId, 999L);

        // then
        List<PendingApproval> result = repository.getPendingApprovals(approverId);
        assertThat(result).hasSize(1); // 원래 데이터는 그대로
    }

    @Test
    @DisplayName("전체 초기화")
    void clearAll() {
        // given
        repository.addPendingApproval(1L, createPendingApproval(100L, 10L, "테스트 1"));
        repository.addPendingApproval(2L, createPendingApproval(101L, 11L, "테스트 2"));

        // when
        repository.clear();

        // then
        assertThat(repository.getPendingApprovals(1L)).isEmpty();
        assertThat(repository.getPendingApprovals(2L)).isEmpty();
    }

    private PendingApproval createPendingApproval(Long requestId, Long requesterId, String title) {
        return PendingApproval.builder()
                .requestId(requestId)
                .requesterId(requesterId)
                .title(title)
                .content("내용")
                .steps(List.of(
                        PendingApproval.StepInfo.builder()
                                .step(1)
                                .approverId(1L)
                                .status("pending")
                                .build()
                ))
                .build();
    }
}
