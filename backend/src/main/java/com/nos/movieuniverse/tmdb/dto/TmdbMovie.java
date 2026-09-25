package com.nos.movieuniverse.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The subset of TMDB's movie payload this application stores. TMDB sends a good
 * deal more; the mapper is lenient here on purpose, unlike the strict one used
 * for the seed file, because a third-party API is free to add fields.
 *
 * @param releaseDate kept as a String deliberately. TMDB returns an empty
 *     string, not null, for films with no known release date, which fails
 *     outright if bound straight to a {@link LocalDate}.
 * @param voteAverage arrives as 0.0 rather than null when there are no votes.
 *     Callers must not store that: see {@link #hasVotes()}.
 */
public record TmdbMovie(
        @JsonProperty("id") Long id,
        @JsonProperty("title") String title,
        @JsonProperty("original_title") String originalTitle,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("overview") String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("runtime") Integer runtime,
        @JsonProperty("vote_average") BigDecimal voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        @JsonProperty("genres") List<TmdbGenre> genres) {

    public TmdbMovie {
        genres = genres == null ? List.of() : List.copyOf(genres);
    }

    /**
     * Whether TMDB holds any votes for this film. The average is only meaningful
     * when this is true; otherwise it must be stored as null so the interface can
     * say "no votes" rather than show a misleading zero.
     */
    public boolean hasVotes() {
        return voteCount != null && voteCount > 0;
    }

    public int voteCountOrZero() {
        return voteCount == null ? 0 : voteCount;
    }

    /** The release date, or null when TMDB has none or sends something unparseable. */
    public LocalDate releaseDateOrNull() {
        if (releaseDate == null || releaseDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(releaseDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
