package com.shopstack.modules.notification.dto;
import com.shopstack.modules.notification.entity.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse { private UUID id, recipientId; private String recipientType, title, message, metadata; private NotificationType type; private NotificationChannel channel; private Boolean isRead; private LocalDateTime sentAt, createdAt; }
