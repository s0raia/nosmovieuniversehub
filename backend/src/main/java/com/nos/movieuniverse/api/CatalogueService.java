package com.nos.movieuniverse.api;

import com.nos.movieuniverse.api.dto.MovieResponse;
import com.nos.movieuniverse.api.dto.PlaylistResponse;
import com.nos.movieuniverse.domain.Movie;
import com.nos.movieuniverse.domain.Playlist;
import com.nos.movieuniverse.rating.CombinedRatingCalculator;
import com.nos.movieuniverse.rating.RatingInput;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.repository.PlaylistRepository;
import com.nos.movieuniverse.repository.UserRatingRepository;
import com.nos.movieuniverse.tmdb.TmdbProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side of the catalogue: turns entities into the shapes the interface wants. */
@Service
@Transactional(readOnly = true)
public class CatalogueService {

    private final MovieRepository movieRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRatingRepository userRatingRepository;
    private final TmdbProperties tmdbProperties;

    public CatalogueService(
            MovieRepository movieRepository,
            PlaylistRepository playlistRepository,
            UserRatingRepository userRatingRepository,
            TmdbProperties tmdbProperties) {
        this.movieRepository = movieRepository;
        this.playlistRepository = playlistRepository;
        this.userRatingRepository = userRatingRepository;
        this.tmdbProperties = tmdbProperties;
    }

    public List<MovieResponse> findAllMovies() {
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return movieRepository.findAll().stream()
                .map(movie -> toResponse(movie, localRatings))
                .sorted((left, right) -> compareByCombinedRating(left, right))
                .toList();
    }

    /** Only live playlists. Soft-deleted ones stay out of the interface. */
    public List<PlaylistResponse> findActivePlaylists() {
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return playlistRepository.findByDeletedAtIsNull().stream()
                .map(playlist -> toResponse(playlist, localRatings))
                .toList();
    }

    private PlaylistResponse toResponse(Playlist playlist, Map<Long, LocalRating> localRatings) {
        List<MovieResponse> films = playlist.getItems().stream()
                .map(item -> toResponse(item.getMovie(), localRatings))
                .toList();

        return new PlaylistResponse(
                playlist.getId(),
                playlist.getExternalId(),
                playlist.getName(),
                playlist.getOwner().getUsername(),
                films.size(),
                films);
    }

    private MovieResponse toResponse(Movie movie, Map<Long, LocalRating> localRatings) {
        LocalRating local = localRatings.getOrDefault(movie.getTmdbId(), LocalRating.NONE);

        RatingInput input = new RatingInput(
                movie.getTmdbVoteCount(), movie.getTmdbVoteAverage(), (int) local.voteCount(), local.average());

        return new MovieResponse(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate() == null ? null : movie.getReleaseDate().getYear(),
                tmdbProperties.posterUrl(movie.getPosterPath()),
                movie.getOverview(),
                movie.getTmdbVoteAverage(),
                movie.getTmdbVoteCount(),
                local.average(),
                local.voteCount(),
                CombinedRatingCalculator.combine(input).orElse(null));
    }

    private Map<Long, LocalRating> loadLocalRatings() {
        Map<Long, LocalRating> byFilm = new HashMap<>();
        for (var aggregate : userRatingRepository.findLocalAggregates()) {
            BigDecimal average = aggregate.getAverage() == null
                    ? null
                    : BigDecimal.valueOf(aggregate.getAverage()).setScale(3, RoundingMode.HALF_UP);
            byFilm.put(aggregate.getTmdbId(), new LocalRating(aggregate.getVoteCount(), average));
        }
        return byFilm;
    }

    /** Highest combined rating first; films with no score at all go last. */
    private int compareByCombinedRating(MovieResponse left, MovieResponse right) {
        return Optional.ofNullable(right.combinedRating())
                .orElse(BigDecimal.valueOf(-1))
                .compareTo(Optional.ofNullable(left.combinedRating()).orElse(BigDecimal.valueOf(-1)));
    }

    private record LocalRating(long voteCount, BigDecimal average) {
        private static final LocalRating NONE = new LocalRating(0, null);
    }
}
