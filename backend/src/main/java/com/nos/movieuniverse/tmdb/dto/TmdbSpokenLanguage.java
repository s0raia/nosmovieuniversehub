package com.nos.movieuniverse.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbSpokenLanguage(
        @JsonProperty("iso_639_1") String iso6391,
        @JsonProperty("english_name") String englishName,
        @JsonProperty("name") String name) {
}
