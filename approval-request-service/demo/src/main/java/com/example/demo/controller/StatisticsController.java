package com.example.demo.controller;

import com.example.demo.dto.StatisticsResponse;
import com.example.demo.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결재 통계 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 전체 결재 통계 조회
     * GET /statistics
     */
    @GetMapping
    public ResponseEntity<StatisticsResponse> getStatistics() {
        log.info("전체 결재 통계 조회 요청");
        StatisticsResponse statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 특정 요청자의 결재 통계 조회
     * GET /statistics/requester/{requesterId}
     */
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<StatisticsResponse> getStatisticsByRequester(
            @PathVariable Long requesterId) {
        log.info("요청자별 통계 조회 요청: requesterId={}", requesterId);
        StatisticsResponse statistics = statisticsService.getStatisticsByRequester(requesterId);
        return ResponseEntity.ok(statistics);
    }
}
