package com.nos.movieuniverse.tmdb.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class TmdbMovieLanguagesTest {

    @Test
    void parsesSpokenLanguagesAndSortsEnglishNames() throws Exception {
        String json =
                """
                {
                  "id": 545611,
                  "title": "Everything Everywhere All at Once",
                  "original_language": "en",
                  "spoken_languages": [
                    { "iso_639_1": "en", "english_name": "English", "name": "English" },
                    { "iso_639_1": "zh", "english_name": "Mandarin", "name": "普通话" },
                    { "iso_639_1": "cn", "english_name": "Cantonese", "name": "广州话 / 廣州話" }
                  ],
                  "vote_count": 100,
                  "vote_average": 8.0
                }
                """;

        TmdbMovie movie = new ObjectMapper().readValue(json, TmdbMovie.class);

        assertThat(movie.originalLanguage()).isEqualTo("en");
        assertThat(movie.spokenLanguageEnglishNames())
                .containsExactly("Cantonese", "English", "Mandarin");
    }
}
