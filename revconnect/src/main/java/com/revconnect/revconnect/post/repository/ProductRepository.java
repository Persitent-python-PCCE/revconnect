package com.revconnect.revconnect.post.repository;

import com.revconnect.revconnect.post.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

}