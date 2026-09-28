package com.revconnect.revconnect.post.dto;

import com.revconnect.revconnect.post.entity.Post;
import java.time.LocalDateTime;

public class PostResponse {

 private Long id;
 private Long userId;
 private Long productId;

 private String username;
 private String photoUrl;
 private String caption;
 private String status;
 private String hashtags;
 private String visibility;
 private String ctaText;
 private String ctaUrl;

 private boolean promotional;
 private boolean pinned;

 private LocalDateTime scheduledAt;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;

 public PostResponse(Post p) {
  this(p, null);
 }

 public PostResponse(Post p, String username) {
  this.id = p.getId();
  this.userId = p.getUserId();
  this.username = username;
  this.productId = p.getProductId();
  this.photoUrl = p.getPhotoUrl();
  this.caption = p.getCaption();
  this.status = p.getStatus();
  this.hashtags = p.getHashtags();
  this.visibility = p.getVisibility();
  this.promotional = p.isPromotional();
  this.ctaText = p.getCtaText();
  this.ctaUrl = p.getCtaUrl();
  this.scheduledAt = p.getScheduledAt();
  this.pinned = p.isPinned();
  this.createdAt = p.getCreatedAt();
  this.updatedAt = p.getUpdatedAt();
 }

 public Long getId() {
  return id;
 }

 public Long getUserId() {
  return userId;
 }

 public Long getProductId() {
  return productId;
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

 public String getHashtags() {
  return hashtags;
 }

 public String getVisibility() {
  return visibility;
 }

 public String getCtaText() {
  return ctaText;
 }

 public String getCtaUrl() {
  return ctaUrl;
 }

 public boolean isPromotional() {
  return promotional;
 }

 public boolean isPinned() {
  return pinned;
 }

 public LocalDateTime getScheduledAt() {
  return scheduledAt;
 }

 public LocalDateTime getCreatedAt() {
  return createdAt;
 }

 public LocalDateTime getUpdatedAt() {
  return updatedAt;
 }
}