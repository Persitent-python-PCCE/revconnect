package com.revconnect.revconnect.post.dto;

import java.time.LocalDateTime;

public class PostRequest {

 private String caption;
 private String hashtags;
 private String visibility;
 private String ctaText;
 private String ctaUrl;

 private boolean promotional;
 private boolean pinned;

 private Long productId;
 private LocalDateTime scheduledAt;

 public String getCaption() {
  return caption;
 }

 public void setCaption(String caption) {
  this.caption = caption;
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
}