package com.revconnect.revconnect.connection.dto;

import java.time.LocalDateTime;

public class ConnectionResponse {
    private Long connectionId;
    private Long userId;
    private String username;
    private LocalDateTime connectedAt;

    public ConnectionResponse() {
    }

    public ConnectionResponse(Long connectionId, Long userId, String username, LocalDateTime connectedAt) {
        this.connectionId = connectionId;
        this.userId = userId;
        this.username = username;
        this.connectedAt = connectedAt;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }
}
