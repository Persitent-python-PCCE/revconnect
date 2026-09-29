package com.revconnect.notification.controller;

import com.revconnect.notification.dto.CreateNotificationRequest;
import com.revconnect.notification.dto.NotificationResponse;
import com.revconnect.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Used by other microservices through Feign/HTTP.
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.create(request));
    }

    /**
     * Uses the same X-User-Id convention currently used by the Post Service.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return ResponseEntity.ok(notificationService.getNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(
                Map.of("count", notificationService.getUnreadCount(userId))
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        if (!notificationService.markAsRead(userId, id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Used by services such as Connection/Interaction when a notification
     * must be removed after a business event changes.
     */
    @DeleteMapping("/internal")
    public ResponseEntity<Void> deleteByTypeAndReferenceId(
            @RequestParam String type,
            @RequestParam Long referenceId) {
        notificationService.deleteByTypeAndReferenceId(type, referenceId);
        return ResponseEntity.noContent().build();
    }
}
