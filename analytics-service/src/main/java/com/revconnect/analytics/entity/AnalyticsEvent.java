package com.revconnect.analytics.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "analytics_events")
public class AnalyticsEvent {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(name = "owner_id", nullable = false)
 private Long ownerId;

 @Column(name = "owner_type", nullable = false, length = 30)
 private String ownerType;

 @Column(name = "metric_type", nullable = false, length = 50)
 private String metricType;

 @Column(name = "post_id")
 private Long postId;

 @Column(name = "metric_value", nullable = false)
 private Long metricValue = 1L;

 public Long getId() {
  return id;
 }

 public void setId(Long id) {
  this.id = id;
 }

 public Long getOwnerId() {
  return ownerId;
 }

 public void setOwnerId(Long ownerId) {
  this.ownerId = ownerId;
 }

 public String getOwnerType() {
  return ownerType;
 }

 public void setOwnerType(String ownerType) {
  this.ownerType = ownerType;
 }

 public String getMetricType() {
  return metricType;
 }

 public void setMetricType(String metricType) {
  this.metricType = metricType;
 }

 public Long getPostId() {
  return postId;
 }

 public void setPostId(Long postId) {
  this.postId = postId;
 }

 public Long getMetricValue() {
  return metricValue;
 }

 public void setMetricValue(Long metricValue) {
  this.metricValue = metricValue;
 }
}