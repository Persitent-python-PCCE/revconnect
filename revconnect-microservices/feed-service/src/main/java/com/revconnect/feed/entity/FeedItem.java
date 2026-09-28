package com.revconnect.feed.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feed_items")
public class FeedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false, unique = true)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String caption;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "post_created_at", nullable = false)
    private LocalDateTime postCreatedAt;

    @Column(name = "post_updated_at", nullable = false)
    private LocalDateTime postUpdatedAt;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPostCreatedAt() { return postCreatedAt; }
    public void setPostCreatedAt(LocalDateTime postCreatedAt) { this.postCreatedAt = postCreatedAt; }
    public LocalDateTime getPostUpdatedAt() { return postUpdatedAt; }
    public void setPostUpdatedAt(LocalDateTime postUpdatedAt) { this.postUpdatedAt = postUpdatedAt; }
    public LocalDateTime getCachedAt() { return cachedAt; }
    public void setCachedAt(LocalDateTime cachedAt) { this.cachedAt = cachedAt; }
}
