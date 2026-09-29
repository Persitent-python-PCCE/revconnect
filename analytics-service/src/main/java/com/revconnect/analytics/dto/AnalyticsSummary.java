package com.revconnect.analytics.dto;

import java.util.Map;

public record AnalyticsSummary(
        Long ownerId,
        String ownerType,
        Map<String, Long> metrics
) {}