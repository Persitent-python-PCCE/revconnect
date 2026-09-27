package com.revconnect.revconnect.notification.controller;

import com.revconnect.revconnect.notification.entity.NotificationPreference;
import com.revconnect.revconnect.notification.service.NotificationService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

 private final NotificationService notificationService;

 public NotificationController(NotificationService notificationService) {
  this.notificationService = notificationService;
 }

 private Long getUserId(Authentication authentication) {
  return (Long) authentication.getPrincipal();
 }

 @GetMapping
 public ResponseEntity<Page<com.revconnect.revconnect.notification.dto.NotificationResponse>> list(
         Authentication authentication,
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "20") int size) {

  return ResponseEntity.ok(
          notificationService.getNotifications(
                  getUserId(authentication),
                  PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)))
          )
  );
 }

 @GetMapping("/unread-count")
 public Map<String, Long> unreadCount(Authentication authentication) {
  return Map.of("count", notificationService.getUnreadCount(getUserId(authentication)));
 }

 @PutMapping("/{notificationId}/read")
 public ResponseEntity<Void> markAsRead(
         @PathVariable Long notificationId,
         Authentication authentication) {

  notificationService.markAsRead(getUserId(authentication), notificationId);
  return ResponseEntity.ok().build();
 }

 @PutMapping("/read-all")
 public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
  notificationService.markAllAsRead(getUserId(authentication));
  return ResponseEntity.ok().build();
 }

 @GetMapping("/preferences")
 public NotificationPreference getPreferences(Authentication authentication) {
  return notificationService.getPreferences(getUserId(authentication));
 }

 @PutMapping("/preferences")
 public NotificationPreference savePreferences(
         Authentication authentication,
         @RequestBody NotificationPreference preferences) {

  return notificationService.savePreferences(getUserId(authentication), preferences);
 }

 @GetMapping("/stream")
 public SseEmitter stream(Authentication authentication) {
  return notificationService.stream(getUserId(authentication));
 }
}