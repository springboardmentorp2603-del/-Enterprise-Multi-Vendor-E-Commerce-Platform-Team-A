package com.shopstack.modules.systemlog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SystemLogResponse {
    private UUID id;
    private String module;
    private String action;
    private String performedByName;
    private String level;
    private String details;
    private LocalDateTime createdAt;
    private LocalDateTime timestamp;
    private String message;
}
