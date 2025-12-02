package com.example.demo.repository;

import com.example.demo.model.PendingApproval;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory 저장소
 * 결재자 ID별로 대기 중인 결재 목록을 관리
 */
@Slf4j
@Repository
public class InMemoryApprovalRepository {

    // Key: approverId, Value: 해당 결재자가 처리해야 할 결재 목록
    private final Map<Long, List<PendingApproval>> pendingApprovals = new ConcurrentHashMap<>();

    /**
     * 결재 대기 목록에 추가
     */
    public void addPendingApproval(Long approverId, PendingApproval approval) {
        pendingApprovals.computeIfAbsent(approverId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(approval);
        log.info("결재 대기 추가: approverId={}, requestId={}", approverId, approval.getRequestId());
    }

    /**
     * 특정 결재자의 대기 목록 조회
     */
    public List<PendingApproval> getPendingApprovals(Long approverId) {
        return pendingApprovals.getOrDefault(approverId, Collections.emptyList());
    }

    /**
     * 특정 결재 건 조회
     */
    public Optional<PendingApproval> findPendingApproval(Long approverId, Long requestId) {
        List<PendingApproval> approvals = pendingApprovals.get(approverId);
        if (approvals == null) {
            return Optional.empty();
        }
        return approvals.stream()
                .filter(a -> a.getRequestId().equals(requestId))
                .findFirst();
    }

    /**
     * 결재 대기 목록에서 제거
     */
    public boolean removePendingApproval(Long approverId, Long requestId) {
        List<PendingApproval> approvals = pendingApprovals.get(approverId);
        if (approvals == null) {
            return false;
        }
        boolean removed = approvals.removeIf(a -> a.getRequestId().equals(requestId));
        if (removed) {
            log.info("결재 대기 제거: approverId={}, requestId={}", approverId, requestId);
        }
        return removed;
    }

    /**
     * 모든 대기 목록 조회 (디버깅용)
     */
    public Map<Long, List<PendingApproval>> getAllPendingApprovals() {
        return new HashMap<>(pendingApprovals);
    }

    /**
     * 전체 초기화 (테스트용)
     */
    public void clear() {
        pendingApprovals.clear();
    }
}
