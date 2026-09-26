package com.revconnect.revconnect.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
    
    @Column(name = "actor_id", nullable = false)
    private Long actorId;
    
    @Column(name = "actor_username", nullable = false)
    private String actorUsername;
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private String type; // e.g., "CONNECTION_REQUEST", "FOLLOW"
    
    @Column(name = "reference_id")
    private Long referenceId;
    
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public Notification() {}
    
    public Notification(Long recipientId, Long actorId, String actorUsername, String message, String type, Long referenceId) {
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.actorUsername = actorUsername;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    
    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
