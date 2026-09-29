package com.revconnect.notification.service;

import com.revconnect.notification.dto.CreateNotificationRequest;
import com.revconnect.notification.dto.NotificationResponse;
import com.revconnect.notification.entity.Notification;
import com.revconnect.notification.repository.NotificationRepository;
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
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification(
                request.getRecipientId(),
                request.getActorId(),
                request.getActorUsername(),
                request.getMessage(),
                request.getType(),
                request.getReferenceId()
        );
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long recipientId, Pageable pageable) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Transactional
    public boolean markAsRead(Long recipientId, Long notificationId) {
        return notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
                .map(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public int markAllAsRead(Long recipientId) {
        return notificationRepository.markAllAsReadByRecipientId(recipientId);
    }

    @Transactional
    public int deleteByTypeAndReferenceId(String type, Long referenceId) {
        return notificationRepository.deleteByTypeAndReferenceId(type, referenceId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipientId(),
                n.getActorId(),
                n.getActorUsername(),
                n.getMessage(),
                n.getType(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
