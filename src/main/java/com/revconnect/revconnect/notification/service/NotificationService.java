package com.revconnect.revconnect.notification.service;

import com.revconnect.revconnect.notification.dto.NotificationResponse;
import com.revconnect.revconnect.notification.entity.Notification;
import com.revconnect.revconnect.notification.repository.NotificationRepository;
import com.revconnect.revconnect.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Transactional
    public void createNotification(Long recipientId, User actor, String message, String type, Long referenceId) {
        Notification notification = new Notification(
                recipientId,
                actor.getId(),
                actor.getUsername(),
                message,
                type,
                referenceId
        );
        notificationRepository.save(notification);
    }
    
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(n -> new NotificationResponse(
                n.getId(), n.getActorId(), n.getActorUsername(), 
                n.getMessage(), n.getType(), n.getReferenceId(), n.isRead(), n.getCreatedAt()
        ));
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }
    
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        notificationRepository.findByIdAndRecipientId(notificationId, userId)
            .ifPresent(n -> {
                n.setRead(true);
                notificationRepository.save(n);
            });
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }
    
    @Transactional
    public void deleteConnectionRequestNotification(Long connectionRequestId) {
        notificationRepository.deleteByReferenceIdAndType(connectionRequestId);
    }
}
