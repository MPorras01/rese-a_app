package com.resenias.reviews.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.RatingStatsDto;
import com.resenias.reviews.dto.ReviewCreateDto;
import com.resenias.reviews.dto.ReviewDto;
import com.resenias.reviews.security.UserPrincipal;
import com.resenias.reviews.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping("/businesses/{businessId}/reviews")
    public Page<ReviewDto> listByBusiness(
            @PathVariable UUID businessId,
            @PageableDefault(size = 10) Pageable pageable) {
        return reviewService.findByBusiness(businessId, pageable);
    }

    @GetMapping("/businesses/{businessId}/reviews/stats")
    public RatingStatsDto stats(@PathVariable UUID businessId) {
        return reviewService.getRatingStats(businessId);
    }

    // ── Authenticated ─────────────────────────────────────────────────────────

    @PostMapping("/businesses/{businessId}/reviews")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable UUID businessId,
            @Valid @RequestBody ReviewCreateDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ReviewDto created = reviewService.createReview(principal.getId(), businessId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/users/me/reviews")
    public Page<ReviewDto> myReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10) Pageable pageable) {
        return reviewService.findByUser(principal.getId(), pageable);
    }

    @PostMapping("/reviews/{reviewId}/report")
    public ResponseEntity<Void> reportReview(
            @PathVariable UUID reviewId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.reportReview(reviewId, principal.getId(), body.get("reason"));
        return ResponseEntity.noContent().build();
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reviews/reported")
    public Page<ReviewDto> reportedReviews(@PageableDefault(size = 10) Pageable pageable) {
        return reviewService.findReported(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/reviews/{reviewId}/hide")
    public ResponseEntity<Void> hideReview(@PathVariable UUID reviewId) {
        reviewService.hideReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/reports/{reportId}/dismiss")
    public ResponseEntity<Void> dismissReport(@PathVariable UUID reportId) {
        reviewService.dismissReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
