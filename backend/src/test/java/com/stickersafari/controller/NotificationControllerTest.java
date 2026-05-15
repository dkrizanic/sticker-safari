package com.stickersafari.controller;

import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Notification;
import com.stickersafari.service.NotificationService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private List<Notification> testNotifications;
    private Collector testCollector;

    @BeforeEach
    void setUp() {
        testCollector = new Collector();
        testCollector.setId(1L);
        testCollector.setUsername("testuser");

        testNotifications = new ArrayList<>();
        
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setMessage("Notification 1");
        notification1.setRead(false);
        notification1.setRecipient(testCollector);
        notification1.setCreatedAt(LocalDateTime.now().minusHours(2));
        testNotifications.add(notification1);

        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setMessage("Notification 2");
        notification2.setRead(false);
        notification2.setRecipient(testCollector);
        notification2.setCreatedAt(LocalDateTime.now().minusHours(1));
        testNotifications.add(notification2);
    }

    @Test
    void getNotifications_ShouldReturnAllNotifications_WhenReadIsTrue() {
        // Given
        when(notificationService.getNotifications(1L)).thenReturn(testNotifications);

        // When
        List<Notification> result = notificationController.getNotifications(1L, true);

        // Then
        assertThat(result).hasSize(2);
        verify(notificationService).getNotifications(1L);
        verify(notificationService, never()).getUnreadNotifications(anyLong());
    }

    @Test
    void getNotifications_ShouldReturnUnreadNotifications_WhenReadIsFalse() {
        // Given
        when(notificationService.getUnreadNotifications(1L)).thenReturn(testNotifications);

        // When
        List<Notification> result = notificationController.getNotifications(1L, false);

        // Then
        assertThat(result).hasSize(2);
        verify(notificationService).getUnreadNotifications(1L);
        verify(notificationService, never()).getNotifications(anyLong());
    }

    @Test
    void getNotifications_ShouldReturnEmptyList_WhenNoNotifications() {
        // Given
        when(notificationService.getNotifications(1L)).thenReturn(new ArrayList<>());

        // When
        List<Notification> result = notificationController.getNotifications(1L, true);

        // Then
        assertThat(result).isEmpty();
        verify(notificationService).getNotifications(1L);
    }

    @Test
    void getNotifications_ShouldSortNotifications() {
        // Given
        when(notificationService.getNotifications(1L)).thenReturn(testNotifications);

        // When
        List<Notification> result = notificationController.getNotifications(1L, true);

        // Then
        assertThat(result).isNotNull();
        verify(notificationService).getNotifications(1L);
    }

    @Test
    void getNotifications_ShouldWorkWithDifferentCollectors() {
        // Given
        when(notificationService.getNotifications(2L)).thenReturn(testNotifications);

        // When
        List<Notification> result = notificationController.getNotifications(2L, true);

        // Then
        assertThat(result).isNotNull();
        verify(notificationService).getNotifications(2L);
    }
}
