package com.stickersafari.service;

import com.stickersafari.entity.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> getNotifications(Long collectorId);
    List<Notification> getUnreadNotifications(Long collectorId);

    Notification saveNotification(Notification notification);
    
    void markAsRead(Long notificationId);
}
