package com.revconnect.product.service;

import com.revconnect.product.dto.ProductRequest;
import com.revconnect.product.dto.ProductResponse;
import com.revconnect.product.entity.Product;
import com.revconnect.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository){this.repository=repository;}
    public ProductResponse create(ProductRequest r){ Product p=toEntity(r); return toResponse(repository.save(p)); }
    public ProductResponse get(Long id){ return repository.findById(id).map(this::toResponse).orElseThrow(() -> new IllegalArgumentException("Product not found")); }
    public List<ProductResponse> byBusiness(Long businessId){ return repository.findByBusinessId(businessId).stream().map(this::toResponse).toList(); }
    public ProductResponse update(Long id, ProductRequest r){ Product p=repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found")); p.setBusinessId(r.businessId()); p.setName(r.name()); p.setDescription(r.description()); p.setPrice(r.price()); p.setImage(r.image()); return toResponse(repository.save(p)); }
    public void delete(Long id){ if(!repository.existsById(id)) throw new IllegalArgumentException("Product not found"); repository.deleteById(id); }
    private Product toEntity(ProductRequest r){ Product p=new Product(); p.setBusinessId(r.businessId()); p.setName(r.name()); p.setDescription(r.description()); p.setPrice(r.price()); p.setImage(r.image()); return p; }
    private ProductResponse toResponse(Product p){ return new ProductResponse(p.getId(),p.getBusinessId(),p.getName(),p.getDescription(),p.getPrice(),p.getImage()); }
}
