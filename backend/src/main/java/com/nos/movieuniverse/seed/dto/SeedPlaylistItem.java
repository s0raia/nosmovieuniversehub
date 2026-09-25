package com.nos.movieuniverse.seed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One film at one position in a seeded playlist.
 *
 * <p>The pair is not unique: pl-01 lists tmdb_id 27205 at both position 1 and
 * position 6. Any importer that treats (playlist, film) as a key will lose a
 * row here.
 *
 * @param position the file's "ordem", one-based.
 */
public record SeedPlaylistItem(
        @JsonProperty("tmdb_id") Long tmdbId,
        @JsonProperty("ordem") int position) {
}
