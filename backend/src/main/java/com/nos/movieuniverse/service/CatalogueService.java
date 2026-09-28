package com.nos.movieuniverse.service;

import com.nos.movieuniverse.dto.MovieDetailResponse;
import com.nos.movieuniverse.dto.MovieResponse;
import com.nos.movieuniverse.dto.PlaylistResponse;
import com.nos.movieuniverse.model.Movie;
import com.nos.movieuniverse.model.Playlist;
import com.nos.movieuniverse.model.AppUser;
import com.nos.movieuniverse.repository.AppUserRepository;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.repository.PlaylistRepository;
import com.nos.movieuniverse.repository.UserRatingRepository;
import com.nos.movieuniverse.tmdb.MovieEnricher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CatalogueService {

    private final MovieRepository movieRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRatingRepository userRatingRepository;
    private final AppUserRepository appUserRepository;
    private final MovieCatalogueMapper mapper;
    private final MovieEnricher movieEnricher;

    public CatalogueService(
            MovieRepository movieRepository,
            PlaylistRepository playlistRepository,
            UserRatingRepository userRatingRepository,
            AppUserRepository appUserRepository,
            MovieCatalogueMapper mapper,
            MovieEnricher movieEnricher) {
        this.movieRepository = movieRepository;
        this.playlistRepository = playlistRepository;
        this.userRatingRepository = userRatingRepository;
        this.appUserRepository = appUserRepository;
        this.mapper = mapper;
        this.movieEnricher = movieEnricher;
    }

    public List<MovieResponse> findAllMovies() {
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return movieRepository.findAllWithGenres().stream()
                .map(movie -> toSummary(movie, localRatings))
                .sorted((left, right) -> compareByCombinedRating(left, right))
                .toList();
    }

    @Transactional
    public MovieDetailResponse findMovieDetail(long tmdbId) {
        Movie movie = movieRepository
                .findById(tmdbId)
                .orElseGet(() -> movieRepository.save(new Movie(tmdbId)));

        boolean missingLanguages = movie.getFetchedAt() != null && movie.getSpokenLanguages() == null;
        if (movie.isStub()
                || movie.getOverview() == null
                || movie.getOverview().isBlank()
                || missingLanguages) {
            movieEnricher.enrich(tmdbId);
            movie = movieRepository.findById(tmdbId).orElseThrow();
        }

        Map<Long, LocalRating> localRatings = loadLocalRatings();
        LocalRating local = localRatings.getOrDefault(tmdbId, LocalRating.NONE);
        return mapper.toDetail(movie, local.average(), local.voteCount());
    }

    public List<PlaylistResponse> findActivePlaylists() {
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return playlistRepository.findByDeletedAtIsNull().stream()
                .map(playlist -> toResponse(playlist, localRatings))
                .toList();
    }

    public List<PlaylistResponse> findPlaylistsForUser(String username) {
        AppUser user = requireUser(username);
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return playlistRepository.findByOwnerIdAndDeletedAtIsNull(user.getId()).stream()
                .map(playlist -> toResponse(playlist, localRatings))
                .toList();
    }

    @Transactional
    public PlaylistResponse createPlaylist(String username, String name) {
        AppUser owner = requireUser(username);
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist name cannot be blank");
        }
        Playlist playlist = playlistRepository.save(new Playlist(trimmed, owner));
        return toResponse(playlist, loadLocalRatings());
    }

    @Transactional
    public PlaylistResponse renamePlaylist(String username, long playlistId, String name) {
        AppUser owner = requireUser(username);
        Playlist playlist = playlistRepository
                .findByIdAndDeletedAtIsNull(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));

        if (!playlist.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your playlist");
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist name cannot be blank");
        }
        playlist.setName(trimmed);
        return toResponse(playlist, loadLocalRatings());
    }

    private AppUser requireUser(String username) {
        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private PlaylistResponse toResponse(Playlist playlist, Map<Long, LocalRating> localRatings) {
        List<MovieResponse> films = playlist.getItems().stream()
                .map(item -> toSummary(item.getMovie(), localRatings))
                .toList();

        return new PlaylistResponse(
                playlist.getId(),
                playlist.getExternalId(),
                playlist.getName(),
                playlist.getOwner().getUsername(),
                films.size(),
                films);
    }

    private MovieResponse toSummary(Movie movie, Map<Long, LocalRating> localRatings) {
        LocalRating local = localRatings.getOrDefault(movie.getTmdbId(), LocalRating.NONE);
        return mapper.toSummary(movie, local.average(), local.voteCount());
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

    private int compareByCombinedRating(MovieResponse left, MovieResponse right) {
        return Optional.ofNullable(right.combinedRating())
                .orElse(BigDecimal.valueOf(-1))
                .compareTo(Optional.ofNullable(left.combinedRating()).orElse(BigDecimal.valueOf(-1)));
    }

    private record LocalRating(long voteCount, BigDecimal average) {
        private static final LocalRating NONE = new LocalRating(0, null);
    }
}
