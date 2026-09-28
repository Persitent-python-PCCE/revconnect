package com.revconnect.notification.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private Long recipientId;
    private Long actorId;
    private String actorUsername;
    private String message;
    private String type;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse(Long id, Long recipientId, Long actorId, String actorUsername,
                                String message, String type, Long referenceId,
                                boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.actorUsername = actorUsername;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getRecipientId() { return recipientId; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Long getReferenceId() { return referenceId; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public void setRead(boolean read) { this.read = read; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
