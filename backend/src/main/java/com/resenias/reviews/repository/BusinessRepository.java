package com.resenias.reviews.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.Business.BusinessStatus;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Page<Business> findByStatus(BusinessStatus status, Pageable pageable);

    List<Business> findByOwnerIdAndStatus(UUID ownerId, BusinessStatus status);

    long countByStatus(BusinessStatus status);

    @Query("""
        SELECT b
        FROM Business b
        WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.description, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.city, '')) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(b.category, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    Page<Business> searchByQuery(@Param("q") String q, Pageable pageable);

    @Query("""
        SELECT b
        FROM Business b
                WHERE b.status = 'APPROVED'
                    AND (:search = ''
               OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(b.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
                    AND (:city = '' OR LOWER(COALESCE(b.city, '')) = LOWER(:city))
                    AND (:category = '' OR LOWER(COALESCE(b.category, '')) = LOWER(:category))
        """)
    Page<Business> findApprovedWithFilters(@Param("search") String search,
                                           @Param("city") String city,
                                           @Param("category") String category,
                                           Pageable pageable);

    Optional<Business> findFirstByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
