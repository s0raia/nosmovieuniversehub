package com.nos.movieuniverse.dto;

import java.math.BigDecimal;
import java.util.List;

/** Full film view for the detail page, including TMDB fields stored locally. */
public record MovieDetailResponse(
        Long tmdbId,
        String title,
        String originalTitle,
        Integer releaseYear,
        String posterUrl,
        String overview,
        Integer runtimeMinutes,
        List<String> genres,
        String originalLanguage,
        List<String> spokenLanguages,
        BigDecimal tmdbVoteAverage,
        int tmdbVoteCount,
        BigDecimal localVoteAverage,
        long localVoteCount,
        BigDecimal combinedRating) {
}
