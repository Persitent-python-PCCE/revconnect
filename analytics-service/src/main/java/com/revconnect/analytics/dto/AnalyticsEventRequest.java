package com.revconnect.analytics.dto;

import jakarta.validation.constraints.*;

public record AnalyticsEventRequest(
        @NotNull Long ownerId,
        @NotBlank String ownerType,
        @NotBlank String metricType,
        Long postId,
        @Min(1) Long value
) {
 public AnalyticsEventRequest {
  if (value == null) {
   value = 1L;
  }
 }
}