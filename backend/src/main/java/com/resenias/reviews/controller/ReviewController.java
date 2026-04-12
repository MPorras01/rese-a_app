package com.resenias.reviews.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resenias.reviews.dto.ReviewSummaryDto;
import com.resenias.reviews.repository.ReviewRepository;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
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
