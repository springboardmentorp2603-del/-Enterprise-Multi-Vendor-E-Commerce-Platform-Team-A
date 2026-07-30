package com.shopstack.modules.notification.controller;
import com.shopstack.common.response.ApiResponse;
import com.shopstack.modules.notification.dto.*;
import com.shopstack.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')") public ApiResponse<NotificationResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request){return service.sendNotification(request);}
    @GetMapping("/user/{recipientId}") @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')") public ApiResponse<List<NotificationResponse>> getNotifications(@PathVariable UUID recipientId){return service.getNotificationsForUser(recipientId);}
    @GetMapping("/user/{recipientId}/unread") @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')") public ApiResponse<List<NotificationResponse>> getUnread(@PathVariable UUID recipientId){return service.getUnreadNotifications(recipientId);}
    @GetMapping("/user/{recipientId}/count") @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')") public ApiResponse<Long> getCount(@PathVariable UUID recipientId){return service.getUnreadCount(recipientId);}
    @PatchMapping("/{id}/read") @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR')") public ApiResponse<Void> markRead(@PathVariable UUID id){return service.markAsRead(id);}
    @PatchMapping("/user/{recipientId}/read-all") @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR')") public ApiResponse<Void> markAllRead(@PathVariable UUID recipientId){return service.markAllAsRead(recipientId);}
}
