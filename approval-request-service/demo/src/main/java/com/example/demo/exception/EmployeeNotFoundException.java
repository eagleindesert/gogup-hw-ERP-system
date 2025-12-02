package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long employeeId) {
        super("직원을 찾을 수 없습니다. employeeId: " + employeeId);
    }

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
