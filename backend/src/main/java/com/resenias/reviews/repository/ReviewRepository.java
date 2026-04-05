package com.resenias.reviews.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.resenias.reviews.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByBusinessIdOrderByCreatedAtDesc(UUID businessId, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    boolean existsByUserIdAndBusinessId(UUID userId, UUID businessId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.business.id = :id")
    Double avgRating(@Param("id") UUID id);

    @Query("""
        SELECT r.rating AS rating, COUNT(r) AS total
        FROM Review r
        WHERE r.business.id = :businessId
          AND r.rating BETWEEN 1 AND 5
        GROUP BY r.rating
        """)
    List<RatingCountProjection> countByRatingRaw(@Param("businessId") UUID businessId);

    default Map<Integer, Long> countByRating(UUID businessId) {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0L);
        }

        for (RatingCountProjection row : countByRatingRaw(businessId)) {
            if (row.getRating() != null) {
                counts.put(row.getRating().intValue(), row.getTotal());
            }
        }

        return counts;
    }

    interface RatingCountProjection {
        Short getRating();

        Long getTotal();
    }
}
