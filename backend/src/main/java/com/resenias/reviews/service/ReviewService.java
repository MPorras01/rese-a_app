package com.resenias.reviews.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resenias.reviews.dto.RatingStatsDto;
import com.resenias.reviews.dto.ReviewCreateDto;
import com.resenias.reviews.dto.ReviewDto;
import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.Product;
import com.resenias.reviews.entity.Review;
import com.resenias.reviews.entity.ReviewReport;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.entity.User.UserStatus;
import com.resenias.reviews.mapper.ReviewMapper;
import com.resenias.reviews.repository.BusinessRepository;
import com.resenias.reviews.repository.ProductRepository;
import com.resenias.reviews.repository.ReviewReportRepository;
import com.resenias.reviews.repository.ReviewRepository;
import com.resenias.reviews.repository.UserRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewReportRepository reportRepository,
                         UserRepository userRepository,
                         BusinessRepository businessRepository,
                         ProductRepository productRepository,
                         ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.productRepository = productRepository;
        this.reviewMapper = reviewMapper;
    }

    @Transactional
    public ReviewDto createReview(UUID userId, UUID businessId, ReviewCreateDto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotVerifiedException("Tu cuenta debe estar activa para publicar reseñas");
        }

        Business business = businessRepository.findById(businessId)
            .filter(b -> b.getStatus() == Business.BusinessStatus.APPROVED)
            .orElseThrow(() -> new BusinessNotFoundException("Negocio no encontrado o no está aprobado"));

        if (reviewRepository.existsByUserIdAndBusinessId(userId, businessId)) {
            throw new DuplicateReviewException("Ya publicaste una reseña para este negocio");
        }

        Product product = null;
        if (dto.productId() != null) {
            product = productRepository.findById(dto.productId())
                .filter(p -> p.getBusiness().getId().equals(businessId))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en este negocio"));
        }

        String[] photos = dto.photos() == null
            ? new String[0]
            : dto.photos().toArray(new String[0]);

        Review review = Review.builder()
            .user(user)
            .business(business)
            .product(product)
            .rating(dto.rating().shortValue())
            .body(dto.body())
            .photos(photos)
            .status(Review.ReviewStatus.ACTIVE)
            .build();

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> findByBusiness(UUID businessId, Pageable pageable) {
        return reviewRepository.findByBusinessIdOrderByCreatedAtDesc(businessId, pageable)
            .map(reviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> findByUser(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(reviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    public RatingStatsDto getRatingStats(UUID businessId) {
        Double avg = reviewRepository.avgRating(businessId);
        long total = reviewRepository.countByRating(businessId)
            .values().stream().mapToLong(Long::longValue).sum();
        return new RatingStatsDto(avg, total, reviewRepository.countByRating(businessId));
    }

    @Transactional
    public void reportReview(UUID reviewId, UUID reporterId, String reason) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        User reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        ReviewReport report = ReviewReport.builder()
            .review(review)
            .reporter(reporter)
            .reason(reason)
            .status("PENDING")
            .build();

        reportRepository.save(report);
    }

    @Transactional
    public void hideReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        review.setStatus(Review.ReviewStatus.HIDDEN);
        reviewRepository.save(review);
    }

    @Transactional
    public void dismissReport(UUID reportId) {
        ReviewReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        report.setStatus("DISMISSED");
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> findReported(Pageable pageable) {
        return reportRepository.findReviewsWithPendingReports(pageable)
            .map(reviewMapper::toDto);
    }
}
