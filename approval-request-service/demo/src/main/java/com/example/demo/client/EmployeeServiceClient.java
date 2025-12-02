package com.example.demo.client;

import com.example.demo.exception.EmployeeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class EmployeeServiceClient {

    private final WebClient webClient;

    public EmployeeServiceClient(@Value("${employee.service.url}") String employeeServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(employeeServiceUrl)
                .build();
    }

    /**
     * 직원 존재 여부 확인
     */
    public boolean existsEmployee(Long employeeId) {
        try {
            Boolean exists = webClient.get()
                    .uri("/employees/{id}/exists", employeeId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Employee Service 호출 실패: employeeId={}", employeeId, e);
            return false;
        }
    }

    /**
     * 직원 존재 여부 검증 (없으면 예외 발생)
     */
    public void validateEmployee(Long employeeId) {
        if (!existsEmployee(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
    }
}
