package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "grpc.client.approval-request-service.address=static://localhost:9091",
        "grpc.client.approval-request-service.negotiationType=plaintext",
        "grpc.server.port=0"
})
@Disabled("gRPC 서버 통합 테스트는 별도로 실행")
class ApprovalProcessingServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
