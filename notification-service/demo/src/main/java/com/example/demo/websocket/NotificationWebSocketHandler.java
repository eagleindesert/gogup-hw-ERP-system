package com.example.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 핸들러 - 직원별 세션 관리 및 메시지 전송
 * 
 * 연결 방식: ws://[host]:8084/ws?id={employeeId}
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 직원 ID별 WebSocket 세션 저장소
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long employeeId = extractEmployeeId(session);
        
        if (employeeId == null) {
            log.warn("WebSocket 연결 거부: employeeId가 없습니다. URI: {}", session.getUri());
            session.close(CloseStatus.BAD_DATA.withReason("Missing 'id' parameter"));
            return;
        }

        // 기존 세션이 있으면 닫고 새 세션으로 교체
        WebSocketSession existingSession = sessions.get(employeeId);
        if (existingSession != null && existingSession.isOpen()) {
            log.info("기존 WebSocket 세션 교체: employeeId={}", employeeId);
            existingSession.close(CloseStatus.NORMAL.withReason("New connection established"));
        }

        sessions.put(employeeId, session);
        log.info("WebSocket 연결 성공: employeeId={}, sessionId={}", employeeId, session.getId());
        
        // 연결 확인 메시지 전송
        sendMessage(employeeId, Map.of(
            "type", "CONNECTION",
            "message", "WebSocket 연결이 성공적으로 수립되었습니다.",
            "employeeId", employeeId
        ));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long employeeId = extractEmployeeId(session);
        
        if (employeeId != null) {
            sessions.remove(employeeId);
            log.info("WebSocket 연결 종료: employeeId={}, status={}", employeeId, status);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long employeeId = extractEmployeeId(session);
        log.debug("메시지 수신: employeeId={}, payload={}", employeeId, message.getPayload());
        
        // 클라이언트로부터 메시지 수신 시 에코백 (ping/pong 용도)
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long employeeId = extractEmployeeId(session);
        log.error("WebSocket 전송 오류: employeeId={}, error={}", employeeId, exception.getMessage());
        
        if (employeeId != null) {
            sessions.remove(employeeId);
        }
        
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 특정 직원에게 메시지 전송
     */
    public boolean sendMessage(Long employeeId, Object message) {
        WebSocketSession session = sessions.get(employeeId);
        
        if (session == null || !session.isOpen()) {
            log.warn("메시지 전송 실패: employeeId={}의 활성 세션 없음", employeeId);
            return false;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.info("메시지 전송 성공: employeeId={}, message={}", employeeId, jsonMessage);
            return true;
        } catch (IOException e) {
            log.error("메시지 전송 실패: employeeId={}, error={}", employeeId, e.getMessage());
            return false;
        }
    }

    /**
     * 연결된 세션 수 조회
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream().filter(WebSocketSession::isOpen).count();
    }

    /**
     * 특정 직원의 연결 상태 확인
     */
    public boolean isConnected(Long employeeId) {
        WebSocketSession session = sessions.get(employeeId);
        return session != null && session.isOpen();
    }

    /**
     * URI에서 employeeId 파라미터 추출
     * 예: ws://localhost:8084/ws?id=123 → 123
     */
    private Long extractEmployeeId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }

        try {
            String idParam = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .getFirst("id");
            
            return idParam != null ? Long.parseLong(idParam) : null;
        } catch (NumberFormatException e) {
            log.warn("employeeId 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}
