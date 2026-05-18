package com.stickersafari.service;

import com.stickersafari.service.impl.AlbumServiceImpl;
import com.stickersafari.service.impl.CollectorServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Circular Dependency Resolution Tests")
class CircularDependencyTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Bug Fix: AlbumService and CollectorService circular dependency should be resolved with @Lazy")
    void testCircularDependencyResolution() {
        // When - Attempting to get both beans from context
        AlbumService albumService = assertDoesNotThrow(() ->
            applicationContext.getBean(AlbumService.class),
            "AlbumService should be created without circular dependency error"
        );

        CollectorService collectorService = assertDoesNotThrow(() ->
            applicationContext.getBean(CollectorService.class),
            "CollectorService should be created without circular dependency error"
        );

        // Then - Both services should be properly initialized
        assertNotNull(albumService, "AlbumService should not be null");
        assertNotNull(collectorService, "CollectorService should not be null");
        assertTrue(albumService instanceof AlbumServiceImpl, "Should be AlbumServiceImpl instance");
        assertTrue(collectorService instanceof CollectorServiceImpl, "Should be CollectorServiceImpl instance");
    }

    @Test
    @DisplayName("Bug Fix: NotificationService bean should be created (@Service annotation)")
    void testNotificationServiceBeanExists() {
        // When - Attempting to get NotificationService bean
        NotificationService notificationService = assertDoesNotThrow(() ->
            applicationContext.getBean(NotificationService.class),
            "NotificationService bean should exist with @Service annotation"
        );

        // Then
        assertNotNull(notificationService, "NotificationService should not be null");
    }

    @Test
    @DisplayName("All service beans should be singleton scoped")
    void testServicesSingletonScope() {
        // When
        AlbumService albumService1 = applicationContext.getBean(AlbumService.class);
        AlbumService albumService2 = applicationContext.getBean(AlbumService.class);

        CollectorService collectorService1 = applicationContext.getBean(CollectorService.class);
        CollectorService collectorService2 = applicationContext.getBean(CollectorService.class);

        NotificationService notificationService1 = applicationContext.getBean(NotificationService.class);
        NotificationService notificationService2 = applicationContext.getBean(NotificationService.class);

        // Then - Same instance should be returned (singleton)
        assertSame(albumService1, albumService2, "AlbumService should be singleton");
        assertSame(collectorService1, collectorService2, "CollectorService should be singleton");
        assertSame(notificationService1, notificationService2, "NotificationService should be singleton");
    }
}
