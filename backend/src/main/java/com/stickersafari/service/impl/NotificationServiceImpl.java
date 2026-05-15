package com.stickersafari.service.impl;

import com.stickersafari.entity.Notification;
import com.stickersafari.repository.NotificationRepository;
import com.stickersafari.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotifications(Long collectorId) {
        List<Notification> notifications = notificationRepository.getNotificationsByRecipientId(collectorId);

        notifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach((notification) -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });

        return notifications;
    }

    @Override
    public List<Notification> getUnreadNotifications(Long collectorId) {
        List<Notification> notifications = notificationRepository.getUnreadNotificationsByRecipientId(collectorId);

        return notifications.stream()
                .filter(notification -> !notification.isRead())
                .map(notification -> {
                    notification.setRead(true);
                    return notificationRepository.save(notification);
                }).collect(Collectors.toList());
    }

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
}
