package com.nos.movieuniverse.seed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The root of a seed file.
 *
 * <p>This package is the quarantine boundary for the Portuguese vocabulary of
 * {@code seed_playlists.json}. That file is supplied by the exercise and must
 * not be edited, so the keys are matched here with {@link JsonProperty} and
 * every field is given an English name. Nothing outside this package should
 * ever see "utilizador" or "estrelas".
 */
public record SeedFile(
        @JsonProperty("versao") String version,
        @JsonProperty("descricao") String description,
        @JsonProperty("utilizadores") List<SeedUser> users,
        @JsonProperty("playlists") List<SeedPlaylist> playlists,
        @JsonProperty("notas") List<SeedRating> ratings) {

    public SeedFile {
        users = users == null ? List.of() : List.copyOf(users);
        playlists = playlists == null ? List.of() : List.copyOf(playlists);
        ratings = ratings == null ? List.of() : List.copyOf(ratings);
    }
}
