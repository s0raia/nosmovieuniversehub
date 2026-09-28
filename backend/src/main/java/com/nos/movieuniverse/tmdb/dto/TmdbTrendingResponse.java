package com.nos.movieuniverse.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbTrendingResponse(@JsonProperty("results") List<TmdbTrendingItem> results) {

    public record TmdbTrendingItem(@JsonProperty("id") Long id) {}
}
