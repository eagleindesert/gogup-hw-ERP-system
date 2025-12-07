package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 결재 통계 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    
    /**
     * 전체 결재 건수
     */
    private long totalRequests;
    
    /**
     * 승인 건수
     */
    private long approvedCount;
    
    /**
     * 반려 건수
     */
    private long rejectedCount;
    
    /**
     * 진행 중 건수
     */
    private long inProgressCount;
    
    /**
     * 승인율 (%)
     */
    private double approvalRate;
    
    /**
     * 반려율 (%)
     */
    private double rejectionRate;
    
    /**
     * 평균 결재 단계 수
     */
    private double avgStepsCount;
    
    /**
     * 직책별 결재 건수
     * key: requesterId, value: 건수
     */
    private Map<Long, Long> requestsByEmployee;
    
    /**
     * 상태별 최근 결재 건수 (최근 10건)
     */
    private Map<String, Long> recentStatusDistribution;
}
