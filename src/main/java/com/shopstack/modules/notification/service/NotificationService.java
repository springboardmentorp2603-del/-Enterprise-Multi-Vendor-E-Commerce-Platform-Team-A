package com.shopstack.modules.notification.service;
import com.shopstack.common.response.ApiResponse;
import com.shopstack.modules.notification.dto.*;
import java.util.*;
public interface NotificationService { ApiResponse<NotificationResponse> sendNotification(SendNotificationRequest request); ApiResponse<List<NotificationResponse>> getNotificationsForUser(UUID recipientId); ApiResponse<List<NotificationResponse>> getUnreadNotifications(UUID recipientId); ApiResponse<Long> getUnreadCount(UUID recipientId); ApiResponse<Void> markAsRead(UUID notificationId); ApiResponse<Void> markAllAsRead(UUID recipientId); }
