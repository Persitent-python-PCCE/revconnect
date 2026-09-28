package com.revconnect.revconnect.notification.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

 @Id
 @Column(name = "user_id")
 private Long userId;

 @Column(nullable = false)
 private boolean likes = true;

 @Column(nullable = false)
 private boolean comments = true;

 @Column(nullable = false)
 private boolean follows = true;

 @Column(nullable = false)
 private boolean connections = true;

 @Column(nullable = false)
 private boolean reposts = true;

 public Long getUserId() {
  return userId;
 }

 public void setUserId(Long userId) {
  this.userId = userId;
 }

 public boolean isLikes() {
  return likes;
 }

 public void setLikes(boolean likes) {
  this.likes = likes;
 }

 public boolean isComments() {
  return comments;
 }

 public void setComments(boolean comments) {
  this.comments = comments;
 }

 public boolean isFollows() {
  return follows;
 }

 public void setFollows(boolean follows) {
  this.follows = follows;
 }

 public boolean isConnections() {
  return connections;
 }

 public void setConnections(boolean connections) {
  this.connections = connections;
 }

 public boolean isReposts() {
  return reposts;
 }

 public void setReposts(boolean reposts) {
  this.reposts = reposts;
 }
}