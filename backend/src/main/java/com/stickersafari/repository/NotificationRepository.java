package com.stickersafari.repository;

import com.stickersafari.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = ?1")
    List<Notification> getNotificationsByRecipientId(Long collectorId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = ?1 AND n.read = false")
    List<Notification> getUnreadNotificationsByRecipientId(Long collectorId);
}
