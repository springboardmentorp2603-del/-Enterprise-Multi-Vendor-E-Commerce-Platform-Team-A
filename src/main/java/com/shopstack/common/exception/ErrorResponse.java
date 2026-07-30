package com.shopstack.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final boolean success;
    private final int status;
    private final String message;
    private final String path;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}