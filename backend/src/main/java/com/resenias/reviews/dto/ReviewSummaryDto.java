package com.resenias.reviews.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewSummaryDto(
    UUID id,
    UUID userId,
    String userName,
    double rating,
    String body,
    String[] photos,
    OffsetDateTime createdAt
) {
}
