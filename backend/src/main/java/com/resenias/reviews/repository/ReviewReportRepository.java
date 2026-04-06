package com.resenias.reviews.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.resenias.reviews.entity.Review;
import com.resenias.reviews.entity.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, UUID> {

    Page<ReviewReport> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Query("""
        SELECT DISTINCT r.review
        FROM ReviewReport r
        WHERE r.status = 'PENDING'
        """)
    Page<Review> findReviewsWithPendingReports(Pageable pageable);
}
