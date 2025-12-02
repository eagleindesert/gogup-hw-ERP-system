package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(Long requestId) {
        super("결재 요청을 찾을 수 없습니다. requestId: " + requestId);
    }

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
