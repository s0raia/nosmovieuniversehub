package com.nos.movieuniverse.tmdb;

import com.nos.movieuniverse.domain.Movie;
import com.nos.movieuniverse.repository.MovieRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Turns placeholder film rows into real ones using TMDB.
 *
 * <p>The importer creates stubs so foreign keys resolve before anything has
 * been fetched. This walks them and hands each to {@link MovieEnricher}, which
 * owns the transaction. The split is not stylistic: see the note on that class.
 *
 * <p>Not transactional itself, on purpose. The loop makes one network call per
 * film, and holding a database connection open across all of them would be
 * wasteful and fragile.
 */
@Service
public class MovieEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(MovieEnrichmentService.class);

    private final MovieRepository movieRepository;
    private final MovieEnricher enricher;
    private final TmdbProperties properties;

    public MovieEnrichmentService(
            MovieRepository movieRepository, MovieEnricher enricher, TmdbProperties properties) {
        this.movieRepository = movieRepository;
        this.enricher = enricher;
        this.properties = properties;
    }

    /** Fills in every film still waiting for its details. */
    public int enrichAllStubs() {
        List<Long> stubIds =
                movieRepository.findByFetchedAtIsNull().stream().map(Movie::getTmdbId).toList();
        if (stubIds.isEmpty()) {
            return 0;
        }

        log.info("Fetching details for {} film(s) from TMDB", stubIds.size());
        int enriched = 0;
        for (Long tmdbId : stubIds) {
            if (enricher.enrich(tmdbId)) {
                enriched++;
            }
            pause();
        }
        log.info("TMDB enrichment finished: {} of {} film(s) updated", enriched, stubIds.size());
        return enriched;
    }

    private void pause() {
        if (properties.throttleMillis() <= 0) {
            return;
        }
        try {
            Thread.sleep(properties.throttleMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
