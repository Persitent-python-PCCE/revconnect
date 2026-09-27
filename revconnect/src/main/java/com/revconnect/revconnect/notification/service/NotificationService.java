package com.revconnect.revconnect.notification.service;

import com.revconnect.revconnect.notification.dto.NotificationResponse;
import com.revconnect.revconnect.notification.entity.*;
import com.revconnect.revconnect.notification.repository.*;
import com.revconnect.revconnect.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class NotificationService {

 private final NotificationRepository repo;
 private final NotificationPreferenceRepository prefs;
 private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

 public NotificationService(NotificationRepository repo, NotificationPreferenceRepository prefs) {
  this.repo = repo;
  this.prefs = prefs;
 }

 @Transactional
 public void createNotification(Long recipientId, User actor, String message, String type, Long ref) {
  if (!enabled(recipientId, type)) {
   return;
  }

  Notification n = repo.save(new Notification(
          recipientId,
          actor.getId(),
          actor.getUsername(),
          message,
          type,
          ref
  ));

  emit(recipientId, toResponse(n));
 }

 private boolean enabled(Long uid, String type) {
  NotificationPreference p = prefs.findById(uid).orElseGet(() -> {
   NotificationPreference x = new NotificationPreference();
   x.setUserId(uid);
   return prefs.save(x);
  });

  return switch (type) {
   case "LIKE" -> p.isLikes();
   case "COMMENT" -> p.isComments();
   case "FOLLOW" -> p.isFollows();
   case "CONNECTION_REQUEST", "CONNECTION_ACCEPTED" -> p.isConnections();
   case "REPOST" -> p.isReposts();
   default -> true;
  };
 }

 public Page<NotificationResponse> getNotifications(Long uid, Pageable page) {
  return repo.findByRecipientIdOrderByCreatedAtDesc(uid, page).map(this::toResponse);
 }

 public long getUnreadCount(Long uid) {
  return repo.countByRecipientIdAndReadFalse(uid);
 }

 @Transactional
 public void markAsRead(Long uid, Long id) {
  repo.findByIdAndRecipientId(id, uid).ifPresent(n -> {
   n.setRead(true);
   repo.save(n);
  });
 }

 @Transactional
 public void markAllAsRead(Long uid) {
  repo.markAllAsReadByRecipientId(uid);
 }

 public NotificationPreference getPreferences(Long uid) {
  return prefs.findById(uid).orElseGet(() -> {
   NotificationPreference p = new NotificationPreference();
   p.setUserId(uid);
   return prefs.save(p);
  });
 }

 public NotificationPreference savePreferences(Long uid, NotificationPreference in) {
  in.setUserId(uid);
  return prefs.save(in);
 }

 public SseEmitter stream(Long uid) {
  SseEmitter e = new SseEmitter(0L); // 0L implies infinite timeout

  emitters.computeIfAbsent(uid, k -> new CopyOnWriteArrayList<>()).add(e);

  e.onCompletion(() -> remove(uid, e));
  e.onTimeout(() -> remove(uid, e));
  e.onError(x -> remove(uid, e));

  try {
   e.send(SseEmitter.event().name("connected").data("connected"));
  } catch (IOException ignored) {
   // Ignored, emitter will be cleaned up on timeout/error
  }

  return e;
 }

 private void emit(Long uid, NotificationResponse n) {
  for (SseEmitter e : emitters.getOrDefault(uid, new CopyOnWriteArrayList<>())) {
   try {
    e.send(SseEmitter.event().name("notification").data(n));
   } catch (Exception ex) {
    remove(uid, e);
   }
  }
 }

 private void remove(Long uid, SseEmitter e) {
  var list = emitters.get(uid);
  if (list != null) {
   list.remove(e);
   if (list.isEmpty()) {
    emitters.remove(uid);
   }
  }
 }

 private NotificationResponse toResponse(Notification n) {
  return new NotificationResponse(
          n.getId(),
          n.getActorId(),
          n.getActorUsername(),
          n.getMessage(),
          n.getType(),
          n.getReferenceId(),
          n.isRead(),
          n.getCreatedAt()
  );
 }

 public void deleteConnectionRequestNotification(Long id) {
  // Assuming deleteByReferenceIdAndType takes (referenceId, type) or just ID based on your repo implementation
  repo.deleteByReferenceIdAndType(id);
 }
}