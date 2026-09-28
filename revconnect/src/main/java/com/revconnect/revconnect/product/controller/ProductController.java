package com.revconnect.revconnect.product.controller;

import java.math.BigDecimal;

public class ProductController {

 private String name;
 private String description;
 private BigDecimal price;
 private String imageUrl;

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
}