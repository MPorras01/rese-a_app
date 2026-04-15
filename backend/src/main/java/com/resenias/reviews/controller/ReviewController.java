package com.resenias.reviews.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.ReviewCreateDto;
import com.resenias.reviews.dto.ReviewSummaryDto;
import com.resenias.reviews.repository.ReviewRepository;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.ReviewService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public ReviewController(ReviewRepository reviewRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<Page<ReviewSummaryDto>> listByBusiness(@RequestParam UUID businessId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewSummaryDto> reviews = reviewRepository.findByBusinessIdOrderByCreatedAtDesc(businessId, pageable)
            .map(review -> new ReviewSummaryDto(
                review.getId(),
                review.getUser() == null ? null : review.getUser().getId(),
                review.getUser() == null ? "Usuario" : review.getUser().getName(),
                review.getRating() == null ? 0 : review.getRating(),
                review.getBody(),
                review.getCreatedAt()));

        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<ReviewSummaryDto> createReview(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ReviewCreateDto dto) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ReviewSummaryDto created = reviewService.createReview(principal.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reviewService.deleteReview(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<MyReviewDto>> myReviews(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MyReviewDto> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(principal.getId(), pageable)
            .map(review -> new MyReviewDto(
                review.getId(),
                review.getBusiness() == null ? null : review.getBusiness().getId(),
                review.getBusiness() == null ? "Negocio" : review.getBusiness().getName(),
                review.getRating() == null ? 0 : review.getRating(),
                review.getBody(),
                review.getCreatedAt()));
        return ResponseEntity.ok(reviews);
    }

    record MyReviewDto(
        java.util.UUID id,
        java.util.UUID businessId,
        String businessName,
        double rating,
        String body,
        java.time.OffsetDateTime createdAt
    ) {}

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(@RequestParam UUID businessId) {
        double avgRating = reviewRepository.avgRating(businessId) == null ? 0 : reviewRepository.avgRating(businessId);
        long totalReviews = reviewRepository.findByBusinessIdOrderByCreatedAtDesc(businessId, PageRequest.of(0, 1)).getTotalElements();
        Map<String, Long> distribution = reviewRepository.countByRating(businessId).entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                entry -> String.valueOf(entry.getKey()),
                Map.Entry::getValue,
                (left, right) -> left,
                java.util.LinkedHashMap::new));

        return ResponseEntity.ok(Map.of(
            "totalReviews", totalReviews,
            "avgRating", avgRating,
            "distribution", distribution
        ));
    }
}
