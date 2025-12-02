package com.example.demo.client;

import com.example.demo.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class NotificationServiceClient {

    private final WebClient webClient;

    public NotificationServiceClient(@Value("${notification.service.url}") String notificationServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }

    /**
     * 알림 전송
     */
    public void sendNotification(NotificationRequest request) {
        try {
            webClient.post()
                    .uri("/notifications/send")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            success -> log.info("알림 전송 성공: requestId={}, employeeId={}",
                                    request.getRequestId(), request.getEmployeeId()),
                            error -> log.error("알림 전송 실패: requestId={}, employeeId={}",
                                    request.getRequestId(), request.getEmployeeId(), error)
                    );
        } catch (Exception e) {
            log.error("Notification Service 호출 실패", e);
        }
    }
}
