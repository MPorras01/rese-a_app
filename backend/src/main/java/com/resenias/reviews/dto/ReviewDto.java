package com.resenias.reviews.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.resenias.reviews.entity.Review.ReviewStatus;

public record ReviewDto(
    UUID id,
    UUID userId,
    String userName,
    String userAvatar,
    UUID businessId,
    UUID productId,
    String productName,
    Integer rating,
    String body,
    List<String> photos,
    ReviewStatus status,
    OffsetDateTime createdAt
) {
}
