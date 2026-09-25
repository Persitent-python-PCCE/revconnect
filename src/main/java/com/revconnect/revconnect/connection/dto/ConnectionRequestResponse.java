package com.revconnect.revconnect.connection.dto;

import com.revconnect.revconnect.connection.entity.ConnectionRequestStatus;

import java.time.LocalDateTime;

public class ConnectionRequestResponse {
    private Long requestId;
    private Long requesterId;
    private String requesterUsername;
    private Long receiverId;
    private String receiverUsername;
    private ConnectionRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConnectionRequestResponse() {
    }

    public ConnectionRequestResponse(Long requestId, Long requesterId, String requesterUsername, Long receiverId, String receiverUsername, ConnectionRequestStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.requesterUsername = requesterUsername;
        this.receiverId = receiverId;
        this.receiverUsername = receiverUsername;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public ConnectionRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionRequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
