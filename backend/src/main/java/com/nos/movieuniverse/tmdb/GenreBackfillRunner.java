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

/** Re-fetches TMDB metadata when enriched films have no genre rows yet. */
@Component
@Order(25)
@ConditionalOnProperty(prefix = "app.tmdb", name = "backfill-missing-genres", havingValue = "true", matchIfMissing = true)
public class GenreBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GenreBackfillRunner.class);

    private final MovieRepository movieRepository;
    private final MovieEnricher movieEnricher;

    public GenreBackfillRunner(MovieRepository movieRepository, MovieEnricher movieEnricher) {
        this.movieRepository = movieRepository;
        this.movieEnricher = movieEnricher;
    }

    @Override
    public void run(ApplicationArguments args) {
        int refreshed = 0;
        for (Movie movie : movieRepository.findAllEnrichedWithGenres()) {
            if (!movie.getGenres().isEmpty()) {
                continue;
            }
            if (movieEnricher.enrich(movie.getTmdbId())) {
                refreshed++;
            }
        }
        if (refreshed > 0) {
            log.info("Backfilled genres for {} film(s) from TMDB", refreshed);
        }
    }
}
