package com.example.demo.config;

import com.example.demo.model.PendingApproval;
import com.example.demo.repository.InMemoryApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 서버 시작 시 Request Service에서 pending 결재 목록을 Pull (REST API 방식)
 * Kafka를 통해 실시간으로 새로운 요청을 수신하지만, 
 * 서버 재시작 시 기존 pending 데이터를 동기화하기 위해 REST 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalDataInitializer {

    private final InMemoryApprovalRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${approval.request.service.url:http://localhost:8082}")
    private String approvalRequestServiceUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("서버 시작 - Request Service에서 pending 결재 목록 동기화 시작");
        
        try {
            syncPendingApprovals();
        } catch (Exception e) {
            log.warn("pending 결재 동기화 실패 (Request Service가 아직 시작되지 않았을 수 있음): {}", e.getMessage());
        }
    }

    /**
     * Request Service에서 pending 결재 목록 동기화 (REST API)
     */
    @SuppressWarnings("unchecked")
    public void syncPendingApprovals() {
        // 기존 데이터 초기화
        repository.clear();
        
        WebClient webClient = webClientBuilder.baseUrl(approvalRequestServiceUrl).build();
        
        try {
            // REST API로 pending 목록 조회
            List<Map<String, Object>> approvals = webClient.get()
                    .uri("/approvals/pending")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            
            if (approvals == null || approvals.isEmpty()) {
                log.info("동기화할 pending 결재가 없습니다.");
                return;
            }

            int syncCount = 0;
            for (Map<String, Object> approval : approvals) {
                List<Map<String, Object>> steps = (List<Map<String, Object>>) approval.get("steps");
                
                // 첫 번째 pending 상태의 결재자 찾기
                Optional<Map<String, Object>> pendingStep = steps.stream()
                        .filter(s -> "pending".equals(s.get("status")))
                        .findFirst();

                if (pendingStep.isPresent()) {
                    Long approverId = ((Number) pendingStep.get().get("approverId")).longValue();

                    // PendingApproval 객체 생성
                    List<PendingApproval.StepInfo> stepInfos = steps.stream()
                            .map(s -> PendingApproval.StepInfo.builder()
                                    .step(((Number) s.get("step")).intValue())
                                    .approverId(((Number) s.get("approverId")).longValue())
                                    .status((String) s.get("status"))
                                    .build())
                            .toList();

                    PendingApproval pendingApproval = PendingApproval.builder()
                            .requestId(((Number) approval.get("requestId")).longValue())
                            .requesterId(((Number) approval.get("requesterId")).longValue())
                            .title((String) approval.get("title"))
                            .content((String) approval.get("content"))
                            .steps(stepInfos)
                            .build();

                    // In-Memory에 저장
                    repository.addPendingApproval(approverId, pendingApproval);
                    syncCount++;
                }
            }
            
            log.info("pending 결재 동기화 완료: {}건", syncCount);
        } catch (Exception e) {
            log.warn("REST API 동기화 실패: {}", e.getMessage());
        }
    }
}
