package com.revconnect.analytics.controller;

import com.revconnect.analytics.dto.*;
import com.revconnect.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

 private final AnalyticsService service;

 public AnalyticsController(AnalyticsService service) {
  this.service = service;
 }

 @PostMapping("/events")
 public ResponseEntity<Void> record(@Valid @RequestBody AnalyticsEventRequest request) {
  service.record(request);
  return ResponseEntity.accepted().build();
 }

 @GetMapping("/{ownerType}/{ownerId}")
 public ResponseEntity<AnalyticsSummary> summary(
         @PathVariable String ownerType,
         @PathVariable Long ownerId) {
  return ResponseEntity.ok(service.summary(ownerId, ownerType));
 }
}