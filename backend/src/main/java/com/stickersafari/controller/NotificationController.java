package com.stickersafari.controller;

import com.stickersafari.entity.Notification;
import com.stickersafari.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{collectorId}")
    public List<Notification> getNotifications(@PathVariable("collectorId") Long collectorId, @RequestParam("read") boolean read) {
        if (read) {
            return sortNotifications(notificationService.getNotifications(collectorId));
        }
        return sortNotifications(notificationService.getUnreadNotifications(collectorId));
    }

    private List<Notification> sortNotifications(List<Notification> notifications) {
        for (int i = 0; i < notifications.size()-1; i++)
        {
            for(int j = 0; j < notifications.size()-i-1; j++)
            {
                if(notifications.get(i).getCreatedAt().isBefore(notifications.get(j + 1).getCreatedAt()))
                {
                    var pom = notifications.get(j + 1);
                    notifications.set(j + 1, notifications.get(j));
                    notifications.set(i, pom);
                }
            }
        }
        return notifications;
    }
}
