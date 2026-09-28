package com.revconnect.revconnect.post.controller;

import com.revconnect.revconnect.post.dto.*;
import com.revconnect.revconnect.post.entity.Product;
import com.revconnect.revconnect.post.repository.ProductRepository;
import com.revconnect.revconnect.user.entity.User;
import com.revconnect.revconnect.user.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

 private final ProductRepository productRepository;
 private final UserRepository userRepository;

 public ProductController(ProductRepository productRepository, UserRepository userRepository) {
  this.productRepository = productRepository;
  this.userRepository = userRepository;
 }

 private Long getUserId(Authentication authentication) {
  return (Long) authentication.getPrincipal();
 }

 @GetMapping("/business/{businessId}")
 public List<ProductResponse> list(@PathVariable Long businessId) {
  return productRepository.findByBusinessIdOrderByCreatedAtDesc(businessId).stream()
          .map(ProductResponse::new)
          .toList();
 }

 @PostMapping
 @PreAuthorize("hasRole('BUSINESS')")
 public ResponseEntity<ProductResponse> create(
         Authentication authentication,
         @RequestBody ProductRequest request) {

  Product savedProduct = productRepository.save(build(getUserId(authentication), request));
  return ResponseEntity.status(HttpStatus.CREATED).body(new ProductResponse(savedProduct));
 }

 @PutMapping("/{productId}")
 @PreAuthorize("hasRole('BUSINESS')")
 public ProductResponse update(
         Authentication authentication,
         @PathVariable Long productId,
         @RequestBody ProductRequest request) {

  Product product = productRepository.findById(productId)
          .orElseThrow(() -> new RuntimeException("Product not found"));

  if (!product.getBusinessId().equals(getUserId(authentication))) {
   throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Not your product");
  }

  product.setName(request.getName());
  product.setDescription(request.getDescription());
  product.setPrice(request.getPrice());
  product.setImageUrl(request.getImageUrl());

  return new ProductResponse(productRepository.save(product));
 }

 @DeleteMapping("/{productId}")
 @PreAuthorize("hasRole('BUSINESS')")
 public ResponseEntity<Void> delete(
         Authentication authentication,
         @PathVariable Long productId) {

  Product product = productRepository.findById(productId)
          .orElseThrow(() -> new RuntimeException("Product not found"));

  if (!product.getBusinessId().equals(getUserId(authentication))) {
   throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Not your product");
  }

  productRepository.delete(product);
  return ResponseEntity.noContent().build();
 }

 private Product build(Long businessId, ProductRequest request) {
  User user = userRepository.findById(businessId)
          .orElseThrow(() -> new RuntimeException("User not found"));

  if (!"BUSINESS".equalsIgnoreCase(user.getAccountType())) {
   throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Business account required");
  }

  Product product = new Product();
  product.setBusinessId(businessId);
  product.setName(request.getName());
  product.setDescription(request.getDescription());
  product.setPrice(request.getPrice());
  product.setImageUrl(request.getImageUrl());

  return product;
 }
}