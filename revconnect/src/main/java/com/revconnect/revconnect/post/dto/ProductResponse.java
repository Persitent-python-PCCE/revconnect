package com.revconnect.revconnect.post.dto;

import com.revconnect.revconnect.post.entity.Product;
import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private Long businessId;

    private String name;
    private String description;
    private String imageUrl;

    private BigDecimal price;

    public ProductResponse(Product p) {
        this.id = p.getId();
        this.businessId = p.getBusinessId();
        this.name = p.getName();
        this.description = p.getDescription();
        this.price = p.getPrice();
        this.imageUrl = p.getImageUrl();
    }

    public Long getId() {
        return id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}