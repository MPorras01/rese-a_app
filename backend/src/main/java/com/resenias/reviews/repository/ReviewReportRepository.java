package com.resenias.reviews.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.resenias.reviews.entity.ReviewReport;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, UUID> {

    Page<ReviewReport> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
