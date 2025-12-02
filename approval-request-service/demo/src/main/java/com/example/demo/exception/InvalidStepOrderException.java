package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStepOrderException extends RuntimeException {

    public InvalidStepOrderException() {
        super("결재 단계는 1부터 시작하여 오름차순으로 구성되어야 합니다.");
    }

    public InvalidStepOrderException(String message) {
        super(message);
    }
}
