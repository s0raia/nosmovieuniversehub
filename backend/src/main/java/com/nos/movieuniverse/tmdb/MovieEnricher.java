package com.nos.movieuniverse.tmdb;

import com.nos.movieuniverse.domain.Genre;
import com.nos.movieuniverse.domain.Movie;
import com.nos.movieuniverse.repository.GenreRepository;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.tmdb.dto.TmdbGenre;
import com.nos.movieuniverse.tmdb.dto.TmdbMovie;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes one film's TMDB details to the database, inside one transaction.
 *
 * <p>Deliberately a separate bean from {@link MovieEnrichmentService} rather
 * than another method on it. Spring's {@code @Transactional} works through a
 * proxy, so a service calling its own annotated method bypasses it entirely and
 * runs with no transaction at all. With {@code open-in-view} disabled that
 * surfaces immediately as a lazy-loading failure on {@link Movie#getGenres()}.
 * Keeping the loop and the unit of work in different beans means every call
 * here crosses the proxy boundary.
 *
 * <p>One transaction per film, not one for the whole catalogue: the loop makes
 * a network call per film and a transaction should not be held open across it.
 */
@Component
public class MovieEnricher {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final TmdbClient tmdbClient;

    public MovieEnricher(
            MovieRepository movieRepository, GenreRepository genreRepository, TmdbClient tmdbClient) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.tmdbClient = tmdbClient;
    }

    /**
     * Fetches one film and stores it.
     *
     * @return false when TMDB had nothing, leaving the row a stub so a later run
     *     can retry.
     */
    @Transactional
    public boolean enrich(long tmdbId) {
        Optional<TmdbMovie> fetched = tmdbClient.fetchMovie(tmdbId);
        if (fetched.isEmpty()) {
            return false;
        }

        TmdbMovie payload = fetched.get();
        Movie movie = movieRepository.findById(tmdbId).orElseGet(() -> new Movie(tmdbId));

        movie.setTitle(payload.title());
        movie.setOriginalTitle(payload.originalTitle());
        movie.setReleaseDate(payload.releaseDateOrNull());
        movie.setOverview(payload.overview());
        movie.setPosterPath(payload.posterPath());
        movie.setRuntime(payload.runtime());

        // The one translation that matters: no votes means no average, not zero.
        movie.setVotes(payload.voteCountOrZero(), payload.hasVotes() ? payload.voteAverage() : null);

        movie.setFetchedAt(OffsetDateTime.now());

        movie.getGenres().clear();
        movie.getGenres().addAll(resolveGenres(payload.genres()));

        movieRepository.save(movie);
        return true;
    }

    private List<Genre> resolveGenres(List<TmdbGenre> incoming) {
        return incoming.stream()
                .map(genre -> genreRepository
                        .findById(genre.id())
                        .orElseGet(() -> genreRepository.save(new Genre(genre.id(), genre.name()))))
                .toList();
    }
}
