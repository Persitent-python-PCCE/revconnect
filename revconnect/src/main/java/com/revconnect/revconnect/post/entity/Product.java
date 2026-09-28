package com.revconnect.revconnect.post.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(name = "business_id", nullable = false)
 private Long businessId;

 @Column(nullable = false, length = 120)
 private String name;

 @Column(length = 500)
 private String description;

 private BigDecimal price;

 @Column(length = 500)
 private String imageUrl;

 @Column(name = "created_at", nullable = false)
 private LocalDateTime createdAt;

 @PrePersist
 protected void onCreate() {
  createdAt = LocalDateTime.now();
 }

 // Getters and Setters

 public Long getId() {
  return id;
 }

 public Long getBusinessId() {
  return businessId;
 }

 public void setBusinessId(Long businessId) {
  this.businessId = businessId;
 }

 public String getName() {
  return name;
 }

 public void setName(String name) {
  this.name = name;
 }

 public String getDescription() {
  return description;
 }

 public void setDescription(String description) {
  this.description = description;
 }

 public BigDecimal getPrice() {
  return price;
 }

 public void setPrice(BigDecimal price) {
  this.price = price;
 }

 public String getImageUrl() {
  return imageUrl;
 }

 public void setImageUrl(String imageUrl) {
  this.imageUrl = imageUrl;
 }

 public LocalDateTime getCreatedAt() {
  return createdAt;
 }
}