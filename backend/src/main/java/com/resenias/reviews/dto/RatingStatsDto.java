package com.resenias.reviews.dto;

import java.util.Map;

public record RatingStatsDto(
    Double average,
    Long total,
    Map<Integer, Long> distribution
) {
}
