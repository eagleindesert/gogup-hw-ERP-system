package com.example.demo.exception;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(Long approverId, Long requestId) {
        super(String.format("결재자 ID %d에 대한 결재 요청 ID %d를 찾을 수 없습니다.", approverId, requestId));
    }

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
