package com.revconnect.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotNull Long businessId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Size(max = 500) String image
) {}