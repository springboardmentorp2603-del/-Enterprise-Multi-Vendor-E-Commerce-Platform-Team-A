package com.shopstack.common.exception;

public class InvalidVendorStateException extends BadRequestException {
    public InvalidVendorStateException(String message) {
        super(message);
    }
}
