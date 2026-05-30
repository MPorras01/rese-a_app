package com.resenias.reviews.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.ProductSummaryDto;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.repository.ProductRepository;
import com.resenias.reviews.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductController(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryDto>> listByBusiness(@RequestParam UUID businessId) {
        List<ProductSummaryDto> products = productRepository.findByBusinessIdAndActiveTrue(businessId).stream()
            .map(product -> new ProductSummaryDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPriceRange(),
                product.isActive()))
            .toList();

        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductSummaryDto> createProduct(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam UUID businessId,
        @RequestBody ProductCreateRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user required");
        }

        ProductSummaryDto created = productService.createProduct(
            businessId,
            principal.getId(),
            request.name(),
            request.description(),
            request.priceRange()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductSummaryDto> updateProduct(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody ProductUpdateRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user required");
        }

        ProductSummaryDto updated = productService.toggleProduct(id, principal.getId(), request.active());
        return ResponseEntity.ok(updated);
    }

    record ProductCreateRequest(String name, String description, String priceRange) {
    }

    record ProductUpdateRequest(boolean active) {
    }
}
