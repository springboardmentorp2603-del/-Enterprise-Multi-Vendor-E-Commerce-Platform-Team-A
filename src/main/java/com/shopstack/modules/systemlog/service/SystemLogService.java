package com.shopstack.modules.systemlog.service;

import com.shopstack.modules.systemlog.dto.SystemLogResponse;
import com.shopstack.modules.systemlog.entity.SystemLog;
import com.shopstack.modules.systemlog.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    @Transactional
    public void log(String module, String action, UUID performedBy, String performedByName, String level, String details) {
        SystemLog entry = SystemLog.builder()
                .module(module)
                .action(action)
                .performedBy(performedBy)
                .performedByName(performedByName)
                .level(level != null ? level : "INFO")
                .details(details)
                .build();
        systemLogRepository.save(entry);
    }

    @Transactional
    public void log(String module, String action, UUID performedBy, String performedByName) {
        log(module, action, performedBy, performedByName, "INFO", null);
    }

    @Transactional(readOnly = true)
    public List<SystemLogResponse> search(LocalDateTime from, LocalDateTime to, String module) {
        return systemLogRepository.search(from, to, module)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

   private SystemLogResponse toResponse(SystemLog log) {
        String composedMessage = "[" + log.getModule() + "] " + log.getAction()
                + (log.getPerformedByName() != null ? " — by " + log.getPerformedByName() : "");

        return SystemLogResponse.builder()
                .id(log.getId())
                .module(log.getModule())
                .action(log.getAction())
                .performedByName(log.getPerformedByName())
                .level(log.getLevel())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .timestamp(log.getCreatedAt())
                .message(composedMessage)
                .build();
    }
}