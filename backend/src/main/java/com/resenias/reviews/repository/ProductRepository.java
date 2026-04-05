package com.resenias.reviews.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resenias.reviews.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByBusinessIdAndActiveTrue(UUID businessId);
}
