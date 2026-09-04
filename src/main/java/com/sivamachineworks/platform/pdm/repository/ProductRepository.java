package com.sivamachineworks.platform.pdm.repository;

import com.sivamachineworks.platform.pdm.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByProductNumber(String productNumber);
    boolean existsByProductNumber(String productNumber);
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByStatus(String status, Pageable pageable);
}
