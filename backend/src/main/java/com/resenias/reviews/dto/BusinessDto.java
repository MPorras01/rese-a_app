package com.resenias.reviews.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.resenias.reviews.entity.Business;

public record BusinessDto(
    UUID id,
    UUID ownerId,
    String ownerName,
    String name,
    String description,
    String category,
    String address,
    String city,
    String phone,
    String email,
    String website,
    Business.BusinessStatus status,
    String rejectionReason,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Double avgRating
) {
}
