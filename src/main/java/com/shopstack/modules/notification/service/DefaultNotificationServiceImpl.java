package com.shopstack.modules.notification.service;

import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.common.response.*;
import com.shopstack.modules.notification.dto.*;
import com.shopstack.modules.notification.entity.*;
import com.shopstack.modules.notification.mapper.NotificationMapper;
import com.shopstack.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service @Slf4j @RequiredArgsConstructor @Transactional
public class DefaultNotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final JavaMailSender emailSender;

    @Override public ApiResponse<NotificationResponse> sendNotification(SendNotificationRequest request) {
        Notification notification = mapper.toEntity(request); notification.setIsRead(false); notification.setSentAt(LocalDateTime.now());
        Notification saved = repository.save(notification);
        switch (saved.getChannel()) {
            case EMAIL -> sendEmail(saved);
            case SMS -> log.info("SMS stub: Twilio not configured"); // TODO integrate Twilio
            case PUSH -> log.info("Push stub: Firebase not configured"); // TODO integrate Firebase
            case IN_APP -> { }
        }
        return ApiResponseBuilder.success("Notification sent successfully", mapper.toResponse(saved));
    }
    private void sendEmail(Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipientId().toString());
        message.setSubject(notification.getTitle()); message.setText(notification.getMessage());
        try { emailSender.send(message); } catch (RuntimeException ex) { log.warn("Email notification could not be sent", ex); }
    }
    @Override @Transactional(readOnly=true) public ApiResponse<List<NotificationResponse>> getNotificationsForUser(UUID id) { return ApiResponseBuilder.success("Notifications fetched successfully", mapper.toResponseList(repository.findByRecipientIdOrderByCreatedAtDesc(id))); }
    @Override @Transactional(readOnly=true) public ApiResponse<List<NotificationResponse>> getUnreadNotifications(UUID id) { return ApiResponseBuilder.success("Unread notifications fetched successfully", mapper.toResponseList(repository.findByRecipientIdAndIsReadFalse(id))); }
    @Override @Transactional(readOnly=true) public ApiResponse<Long> getUnreadCount(UUID id) { return ApiResponseBuilder.success("Unread notification count fetched successfully", repository.countByRecipientIdAndIsReadFalse(id)); }
    @Override public ApiResponse<Void> markAsRead(UUID id) { Notification n=repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("NOTIF_001: Notification not found.")); n.setIsRead(true); repository.save(n); return ApiResponseBuilder.success("Notification marked as read"); }
    @Override public ApiResponse<Void> markAllAsRead(UUID id) { List<Notification> list=repository.findByRecipientIdAndIsReadFalse(id); list.forEach(n -> n.setIsRead(true)); repository.saveAll(list); return ApiResponseBuilder.success("Notifications marked as read"); }
}
