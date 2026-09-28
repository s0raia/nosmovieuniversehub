package com.nos.movieuniverse.tmdb;

import com.nos.movieuniverse.model.Movie;
import com.nos.movieuniverse.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Re-fetches TMDB metadata when enriched films have no spoken-language data yet. */
@Component
@Order(26)
@ConditionalOnProperty(prefix = "app.tmdb", name = "backfill-missing-languages", havingValue = "true", matchIfMissing = true)
public class LanguageBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LanguageBackfillRunner.class);

    private final MovieRepository movieRepository;
    private final MovieEnricher movieEnricher;

    public LanguageBackfillRunner(MovieRepository movieRepository, MovieEnricher movieEnricher) {
        this.movieRepository = movieRepository;
        this.movieEnricher = movieEnricher;
    }

    @Override
    public void run(ApplicationArguments args) {
        int refreshed = 0;
        for (Movie movie : movieRepository.findByFetchedAtIsNotNullAndSpokenLanguagesIsNull()) {
            if (movieEnricher.enrich(movie.getTmdbId())) {
                refreshed++;
            }
        }
        if (refreshed > 0) {
            log.info("Backfilled spoken languages for {} film(s) from TMDB", refreshed);
        }
    }
}
