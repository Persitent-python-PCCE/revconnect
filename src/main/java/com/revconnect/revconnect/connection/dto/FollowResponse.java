package com.revconnect.revconnect.connection.dto;

import java.time.LocalDateTime;

public class FollowResponse {
    private Long followId;
    private Long userId;
    private String username;
    private LocalDateTime followedAt;

    public FollowResponse() {
    }

    public FollowResponse(Long followId, Long userId, String username, LocalDateTime followedAt) {
        this.followId = followId;
        this.userId = userId;
        this.username = username;
        this.followedAt = followedAt;
    }

    public Long getFollowId() {
        return followId;
    }

    public void setFollowId(Long followId) {
        this.followId = followId;
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

    public LocalDateTime getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(LocalDateTime followedAt) {
        this.followedAt = followedAt;
    }
}
