package com.example.demo.service;

import com.example.demo.document.ApprovalRequestDocument;
import com.example.demo.dto.StatisticsResponse;
import com.example.demo.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 결재 통계 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ApprovalRequestRepository approvalRequestRepository;

    /**
     * 전체 결재 통계 조회
     */
    public StatisticsResponse getStatistics() {
        log.info("결재 통계 조회 시작");

        List<ApprovalRequestDocument> allRequests = approvalRequestRepository.findAll();

        if (allRequests.isEmpty()) {
            return StatisticsResponse.builder()
                    .totalRequests(0)
                    .approvedCount(0)
                    .rejectedCount(0)
                    .inProgressCount(0)
                    .approvalRate(0.0)
                    .rejectionRate(0.0)
                    .avgStepsCount(0.0)
                    .requestsByEmployee(new HashMap<>())
                    .recentStatusDistribution(new HashMap<>())
                    .build();
        }

        long totalRequests = allRequests.size();
        
        // 상태별 집계
        long approvedCount = allRequests.stream()
                .filter(doc -> "approved".equals(doc.getFinalStatus()))
                .count();
        
        long rejectedCount = allRequests.stream()
                .filter(doc -> "rejected".equals(doc.getFinalStatus()))
                .count();
        
        long inProgressCount = allRequests.stream()
                .filter(doc -> "in_progress".equals(doc.getFinalStatus()))
                .count();

        // 승인율, 반려율 계산
        long completedCount = approvedCount + rejectedCount;
        double approvalRate = completedCount > 0 ? (approvedCount * 100.0 / completedCount) : 0.0;
        double rejectionRate = completedCount > 0 ? (rejectedCount * 100.0 / completedCount) : 0.0;

        // 평균 결재 단계 수
        double avgStepsCount = allRequests.stream()
                .mapToInt(doc -> doc.getSteps() != null ? doc.getSteps().size() : 0)
                .average()
                .orElse(0.0);

        // 요청자별 결재 건수
        Map<Long, Long> requestsByEmployee = allRequests.stream()
                .collect(Collectors.groupingBy(
                        ApprovalRequestDocument::getRequesterId,
                        Collectors.counting()
                ));

        // 상태별 분포 (최근 건 기준)
        Map<String, Long> recentStatusDistribution = allRequests.stream()
                .collect(Collectors.groupingBy(
                        ApprovalRequestDocument::getFinalStatus,
                        Collectors.counting()
                ));

        log.info("통계 계산 완료: total={}, approved={}, rejected={}, inProgress={}", 
                totalRequests, approvedCount, rejectedCount, inProgressCount);

        return StatisticsResponse.builder()
                .totalRequests(totalRequests)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .inProgressCount(inProgressCount)
                .approvalRate(Math.round(approvalRate * 100.0) / 100.0)  // 소수점 2자리
                .rejectionRate(Math.round(rejectionRate * 100.0) / 100.0)
                .avgStepsCount(Math.round(avgStepsCount * 100.0) / 100.0)
                .requestsByEmployee(requestsByEmployee)
                .recentStatusDistribution(recentStatusDistribution)
                .build();
    }

    /**
     * 특정 요청자의 통계 조회
     */
    public StatisticsResponse getStatisticsByRequester(Long requesterId) {
        log.info("요청자별 통계 조회: requesterId={}", requesterId);

        List<ApprovalRequestDocument> requests = approvalRequestRepository
                .findByRequesterId(requesterId);

        if (requests.isEmpty()) {
            return StatisticsResponse.builder()
                    .totalRequests(0)
                    .approvedCount(0)
                    .rejectedCount(0)
                    .inProgressCount(0)
                    .approvalRate(0.0)
                    .rejectionRate(0.0)
                    .avgStepsCount(0.0)
                    .build();
        }

        long totalRequests = requests.size();
        
        long approvedCount = requests.stream()
                .filter(doc -> "approved".equals(doc.getFinalStatus()))
                .count();
        
        long rejectedCount = requests.stream()
                .filter(doc -> "rejected".equals(doc.getFinalStatus()))
                .count();
        
        long inProgressCount = requests.stream()
                .filter(doc -> "in_progress".equals(doc.getFinalStatus()))
                .count();

        long completedCount = approvedCount + rejectedCount;
        double approvalRate = completedCount > 0 ? (approvedCount * 100.0 / completedCount) : 0.0;
        double rejectionRate = completedCount > 0 ? (rejectedCount * 100.0 / completedCount) : 0.0;

        double avgStepsCount = requests.stream()
                .mapToInt(doc -> doc.getSteps() != null ? doc.getSteps().size() : 0)
                .average()
                .orElse(0.0);

        return StatisticsResponse.builder()
                .totalRequests(totalRequests)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .inProgressCount(inProgressCount)
                .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                .rejectionRate(Math.round(rejectionRate * 100.0) / 100.0)
                .avgStepsCount(Math.round(avgStepsCount * 100.0) / 100.0)
                .build();
    }
}
