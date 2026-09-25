package com.nos.movieuniverse.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbGenre(@JsonProperty("id") Long id, @JsonProperty("name") String name) {
}
