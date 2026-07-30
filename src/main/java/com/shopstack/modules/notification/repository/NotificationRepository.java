package com.shopstack.modules.notification.repository;
import com.shopstack.modules.notification.entity.Notification;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    List<Notification> findByRecipientIdAndIsReadFalse(UUID recipientId);
    long countByRecipientIdAndIsReadFalse(UUID recipientId);
}
