package com.stickersafari.service;

import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Notification;
import com.stickersafari.repository.NotificationRepository;
import com.stickersafari.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Collector testCollector;
    private List<Notification> testNotifications;

    @BeforeEach
    void setUp() {
        testCollector = new Collector();
        testCollector.setId(1L);
        testCollector.setUsername("testuser");

        testNotifications = new ArrayList<>();
        
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setMessage("Test notification 1");
        notification1.setRead(false);
        notification1.setRecipient(testCollector);
        notification1.setCreatedAt(LocalDateTime.now());
        testNotifications.add(notification1);

        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setMessage("Test notification 2");
        notification2.setRead(false);
        notification2.setRecipient(testCollector);
        notification2.setCreatedAt(LocalDateTime.now());
        testNotifications.add(notification2);

        Notification notification3 = new Notification();
        notification3.setId(3L);
        notification3.setMessage("Test notification 3");
        notification3.setRead(true);
        notification3.setRecipient(testCollector);
        notification3.setCreatedAt(LocalDateTime.now());
        testNotifications.add(notification3);
    }

    @Test
    void getNotifications_ShouldReturnAllNotifications_AndMarkUnreadAsRead() {
        // Given
        when(notificationRepository.findByRecipientId(1L)).thenReturn(testNotifications);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Notification> result = notificationService.getNotifications(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(Notification::isRead);
        verify(notificationRepository).findByRecipientId(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class)); // Only 2 were unread
    }

    @Test
    void getUnreadNotifications_ShouldReturnOnlyUnreadNotifications_AndMarkAsRead() {
        // Given
        List<Notification> unreadNotifications = new ArrayList<>();
        unreadNotifications.add(testNotifications.get(0));
        unreadNotifications.add(testNotifications.get(1));

        when(notificationRepository.findByRecipientIdAndReadFalse(1L)).thenReturn(unreadNotifications);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Notification> result = notificationService.getUnreadNotifications(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Notification::isRead);
        verify(notificationRepository).findByRecipientIdAndReadFalse(1L);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void getNotifications_ShouldReturnEmptyList_WhenNoNotifications() {
        // Given
        when(notificationRepository.findByRecipientId(1L)).thenReturn(new ArrayList<>());

        // When
        List<Notification> result = notificationService.getNotifications(1L);

        // Then
        assertThat(result).isEmpty();
        verify(notificationRepository).findByRecipientId(1L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void saveNotification_ShouldSaveAndReturnNotification() {
        // Given
        Notification newNotification = new Notification();
        newNotification.setMessage("New notification");
        newNotification.setRecipient(testCollector);

        when(notificationRepository.save(newNotification)).thenReturn(newNotification);

        // When
        Notification result = notificationService.saveNotification(newNotification);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("New notification");
        verify(notificationRepository).save(newNotification);
    }

    @Test
    void getNotifications_ShouldNotMarkAlreadyReadNotifications() {
        // Given
        List<Notification> allReadNotifications = new ArrayList<>();
        Notification readNotification = new Notification();
        readNotification.setId(1L);
        readNotification.setRead(true);
        readNotification.setRecipient(testCollector);
        allReadNotifications.add(readNotification);

        when(notificationRepository.findByRecipientId(1L)).thenReturn(allReadNotifications);

        // When
        List<Notification> result = notificationService.getNotifications(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(notificationRepository).findByRecipientId(1L);
        verify(notificationRepository, never()).save(any(Notification.class)); // No saves since already read
    }
}
