package com.shopstack.modules.notification.dto;
import com.shopstack.modules.notification.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendNotificationRequest {
    @NotNull private UUID recipientId; @NotBlank private String recipientType; @NotNull private NotificationType type; @NotNull private NotificationChannel channel; @NotBlank private String title; @NotBlank private String message; private String metadata;
}
