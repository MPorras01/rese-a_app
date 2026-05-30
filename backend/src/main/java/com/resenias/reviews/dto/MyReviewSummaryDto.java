package com.resenias.reviews.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MyReviewSummaryDto(
    UUID id,
    UUID businessId,
    String businessName,
    double rating,
    String body,
    OffsetDateTime createdAt
) {
}