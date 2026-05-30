package com.resenias.reviews.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resenias.reviews.dto.ReviewCreateDto;
import com.resenias.reviews.dto.ReviewSummaryDto;
import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.Product;
import com.resenias.reviews.entity.Review;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.BusinessRepository;
import com.resenias.reviews.repository.ProductRepository;
import com.resenias.reviews.repository.ReviewRepository;
import com.resenias.reviews.repository.UserRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BusinessRepository businessRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ReviewSummaryDto createReview(UUID authorId, ReviewCreateDto dto) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (author.getStatus() != User.UserStatus.ACTIVE) {
            throw new AccessDeniedException("Tu cuenta debe estar activa para escribir reseñas");
        }

        Business business = businessRepository.findById(dto.businessId())
            .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        if (business.getStatus() != Business.BusinessStatus.APPROVED) {
            throw new RuntimeException("Solo se pueden reseñar negocios aprobados");
        }

        // Evitar reseñas duplicadas del mismo usuario al mismo negocio
        if (reviewRepository.existsByUserIdAndBusinessId(authorId, dto.businessId())) {
            throw new DuplicateReviewException("Ya escribiste una reseña para este negocio");
        }

        Product product = null;
        if (dto.productId() != null) {
            product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Verificar que el producto pertenece al negocio
            if (!product.getBusiness().getId().equals(dto.businessId())) {
                throw new RuntimeException("El producto no pertenece a este negocio");
            }
        }

        Review review = Review.builder()
            .user(author)
            .business(business)
            .product(product)
            .rating(dto.rating())
            .body(dto.body())
            .status(Review.ReviewStatus.ACTIVE)
            .build();

        Review saved = reviewRepository.save(review);

        return new ReviewSummaryDto(
            saved.getId(),
            author.getId(),
            author.getName(),
            saved.getRating() == null ? 0 : saved.getRating(),
            saved.getBody(),
            saved.getCreatedAt()
        );
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID requesterId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isAuthor = review.getUser() != null && review.getUser().getId().equals(requesterId);
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta reseña");
        }

        reviewRepository.delete(review);
    }
}
