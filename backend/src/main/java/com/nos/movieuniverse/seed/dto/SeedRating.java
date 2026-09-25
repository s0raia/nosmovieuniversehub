package com.nos.movieuniverse.seed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * One user's rating of one film, from the file's "notas".
 *
 * @param stars 1 to 10 inclusive.
 * @param ratedOn the file's "data", a plain calendar day such as "2026-09-01".
 */
public record SeedRating(
        @JsonProperty("utilizador") String username,
        @JsonProperty("tmdb_id") Long tmdbId,
        @JsonProperty("estrelas") short stars,
        @JsonProperty("data") LocalDate ratedOn) {
}
