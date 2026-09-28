package com.revconnect.revconnect.post.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(name = "user_id", nullable = false)
 private Long userId;

 @Column(name = "photo_url", nullable = false, length = 500)
 private String photoUrl;

 @Column(nullable = false, columnDefinition = "TEXT")
 private String caption;

 @Column(nullable = false, length = 20)
 private String status;

 @Column(columnDefinition = "TEXT")
 private String hashtags;

 @Column(nullable = false, length = 20)
 private String visibility = "PUBLIC";

 @Column(name = "is_promotional", nullable = false)
 private boolean promotional = false;

 @Column(name = "cta_text", length = 100)
 private String ctaText;

 @Column(name = "cta_url", length = 500)
 private String ctaUrl;

 @Column(name = "product_id")
 private Long productId;

 @Column(name = "scheduled_at")
 private LocalDateTime scheduledAt;

 @Column(nullable = false)
 private boolean pinned = false;

 @Column(name = "created_at", nullable = false)
 private LocalDateTime createdAt;

 @Column(name = "updated_at", nullable = false)
 private LocalDateTime updatedAt;

 @PrePersist
 protected void onCreate() {
  LocalDateTime n = LocalDateTime.now();
  createdAt = n;
  updatedAt = n;
  if (status == null) {
   status = "PUBLISHED";
  }
  if (visibility == null) {
   visibility = "PUBLIC";
  }
 }

 @PreUpdate
 protected void onUpdate() {
  updatedAt = LocalDateTime.now();
 }

 // Getters and Setters

 public Long getId() {
  return id;
 }

 public Long getUserId() {
  return userId;
 }

 public void setUserId(Long userId) {
  this.userId = userId;
 }

 public String getPhotoUrl() {
  return photoUrl;
 }

 public void setPhotoUrl(String photoUrl) {
  this.photoUrl = photoUrl;
 }

 public String getCaption() {
  return caption;
 }

 public void setCaption(String caption) {
  this.caption = caption;
 }

 public String getStatus() {
  return status;
 }

 public void setStatus(String status) {
  this.status = status;
 }

 public String getHashtags() {
  return hashtags;
 }

 public void setHashtags(String hashtags) {
  this.hashtags = hashtags;
 }

 public String getVisibility() {
  return visibility;
 }

 public void setVisibility(String visibility) {
  this.visibility = visibility;
 }

 public boolean isPromotional() {
  return promotional;
 }

 public void setPromotional(boolean promotional) {
  this.promotional = promotional;
 }

 public String getCtaText() {
  return ctaText;
 }

 public void setCtaText(String ctaText) {
  this.ctaText = ctaText;
 }

 public String getCtaUrl() {
  return ctaUrl;
 }

 public void setCtaUrl(String ctaUrl) {
  this.ctaUrl = ctaUrl;
 }

 public Long getProductId() {
  return productId;
 }

 public void setProductId(Long productId) {
  this.productId = productId;
 }

 public LocalDateTime getScheduledAt() {
  return scheduledAt;
 }

 public void setScheduledAt(LocalDateTime scheduledAt) {
  this.scheduledAt = scheduledAt;
 }

 public boolean isPinned() {
  return pinned;
 }

 public void setPinned(boolean pinned) {
  this.pinned = pinned;
 }

 public LocalDateTime getCreatedAt() {
  return createdAt;
 }

 public LocalDateTime getUpdatedAt() {
  return updatedAt;
 }
}