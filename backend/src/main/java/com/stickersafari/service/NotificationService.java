package com.stickersafari.service;

import com.stickersafari.entity.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> getNotifications(Long collectorId);
    List<Notification> getUnreadNotifications(Long collectorId);
    List<Notification> getReadNotifications(Long collectorId);
    List<Notification> getNotificationsByFilter(Long collectorId, String filter);

    Notification saveNotification(Notification notification);
    
    void markAsRead(Long notificationId);
    
    void deleteNotification(Long notificationId);
    
    boolean notificationExists(Long collectorId, String message);
}
