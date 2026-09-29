package com.revconnect.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long businessId,
        String name,
        String description,
        BigDecimal price,
        String image
) {}