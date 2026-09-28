package com.nos.movieuniverse.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * A film as the interface needs it.
 *
 * <p>Every rating field is nullable, and null always means "not enough
 * information" rather than zero. The distinction is a requirement, not a
 * detail: a film nobody has voted on must not be shown as rated 0.
 *
 * @param releaseYear null when TMDB has no release date.
 * @param tmdbVoteAverage null exactly when {@code tmdbVoteCount} is zero.
 * @param combinedRating null when neither TMDB nor local users have rated it.
 */
public record MovieResponse(
        Long tmdbId,
        String title,
        Integer releaseYear,
        String posterUrl,
        String overview,
        BigDecimal tmdbVoteAverage,
        int tmdbVoteCount,
        BigDecimal localVoteAverage,
        long localVoteCount,
        BigDecimal combinedRating,
        List<String> genres) {
}
