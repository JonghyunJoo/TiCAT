package com.example.paymentservice.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 에러 메시지를 상위 클래스 생성자에 전달
        this.errorCode = errorCode;
    }
}
