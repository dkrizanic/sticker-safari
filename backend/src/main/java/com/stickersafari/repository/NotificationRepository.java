package com.stickersafari.repository;

import com.stickersafari.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientId(Long collectorId);

    List<Notification> findByRecipientIdAndReadFalse(Long collectorId);
    
    List<Notification> findByRecipientIdAndReadTrue(Long collectorId);
    
    List<Notification> findByRecipientIdAndMessageAndReadFalse(Long recipientId, String message);
}
