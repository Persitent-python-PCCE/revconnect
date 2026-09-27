package com.revconnect.revconnect.notification.repository;

import com.revconnect.revconnect.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    long countByRecipientIdAndReadFalse(Long recipientId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipientId = :recipientId")
    void markAllAsReadByRecipientId(Long recipientId);
    
    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.type = 'CONNECTION_REQUEST' AND n.referenceId = :referenceId")
    void deleteByReferenceIdAndType(Long referenceId);
}
