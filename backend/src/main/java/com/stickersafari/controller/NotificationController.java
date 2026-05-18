package com.stickersafari.controller;

import com.stickersafari.entity.Notification;
import com.stickersafari.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{collectorId}")
    public List<Notification> getNotifications(
            @PathVariable("collectorId") Long collectorId,
            @RequestParam(defaultValue = "unread") String filter) {
        
        log.debug("Getting notifications for collector {}, filter={}", collectorId, filter);
        return notificationService.getNotificationsByFilter(collectorId, filter);
    }
    
    @PutMapping("/{notificationId}/read")
    public void markNotificationAsRead(@PathVariable("notificationId") Long notificationId) {
        log.debug("Marking notification {} as read", notificationId);
        notificationService.markAsRead(notificationId);
    }
    
    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable("notificationId") Long notificationId) {
        log.debug("Deleting notification {}", notificationId);
        notificationService.deleteNotification(notificationId);
    }
}
