package com.shopstack.modules.notification.mapper;
import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.notification.dto.*;
import com.shopstack.modules.notification.entity.Notification;
import org.mapstruct.Mapper;
import java.util.List;
@Mapper(config=MapStructConfig.class)
public interface NotificationMapper { NotificationResponse toResponse(Notification notification); List<NotificationResponse> toResponseList(List<Notification> list); Notification toEntity(SendNotificationRequest request); }
