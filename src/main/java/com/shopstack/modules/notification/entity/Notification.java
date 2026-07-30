package com.shopstack.modules.notification.entity;
import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity @Table(name="notifications") @Getter @Setter @NoArgsConstructor
public class Notification extends BaseEntity {
    @Column(name="recipient_id", nullable=false) private UUID recipientId;
    @Column(name="recipient_type", nullable=false, length=20) private String recipientType;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private NotificationType type;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10) private NotificationChannel channel;
    @Column(nullable=false) private String title;
    @Column(nullable=false, columnDefinition="TEXT") private String message;
    @Column(name="is_read", nullable=false) private Boolean isRead = false;
    private LocalDateTime sentAt;
    @Column(columnDefinition="TEXT") private String metadata;
}
