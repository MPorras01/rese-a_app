package com.resenias.reviews.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resenias.reviews.dto.ProductSummaryDto;
import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.Product;
import com.resenias.reviews.repository.BusinessRepository;
import com.resenias.reviews.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;

    public ProductService(ProductRepository productRepository, BusinessRepository businessRepository) {
        this.productRepository = productRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public ProductSummaryDto createProduct(UUID businessId, UUID ownerId, String name, String description, String priceRange) {
        Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        boolean isOwner = business.getOwner() != null && business.getOwner().getId().equals(ownerId);
        if (!isOwner) {
            throw new AccessDeniedException("Only owner can create products for this business");
        }

        Product product = Product.builder()
            .business(business)
            .name(name)
            .description(description)
            .priceRange(priceRange)
            .active(true)
            .build();

        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    @Transactional
    public ProductSummaryDto toggleProduct(UUID productId, UUID ownerId, boolean active) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        Business business = product.getBusiness();
        boolean isOwner = business.getOwner() != null && business.getOwner().getId().equals(ownerId);
        if (!isOwner) {
            throw new AccessDeniedException("Only owner can update products");
        }

        product.setActive(active);
        Product updated = productRepository.save(product);
        return toDto(updated);
    }

    private ProductSummaryDto toDto(Product product) {
        return new ProductSummaryDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPriceRange(),
            product.isActive()
        );
    }
}
