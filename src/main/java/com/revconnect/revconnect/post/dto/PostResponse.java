package com.revconnect.revconnect.post.dto;

import com.revconnect.revconnect.post.entity.Post;

import java.time.LocalDateTime;

public class PostResponse {

    private Long id;
    private Long userId;
    private String username;
    private String photoUrl;
    private String caption;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.photoUrl = post.getPhotoUrl();
        this.caption = post.getCaption();
        this.status = post.getStatus();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }

    public PostResponse(Post post, String username) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.username = username;
        this.photoUrl = post.getPhotoUrl();
        this.caption = post.getCaption();
        this.status = post.getStatus();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getCaption() {
        return caption;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}