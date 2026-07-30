package com.shopstack.common.exception;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(message);
    }
}