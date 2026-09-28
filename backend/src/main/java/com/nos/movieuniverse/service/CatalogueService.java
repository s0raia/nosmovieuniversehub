package com.nos.movieuniverse.service;

import com.nos.movieuniverse.dto.MovieDetailResponse;
import com.nos.movieuniverse.dto.MovieResponse;
import com.nos.movieuniverse.dto.PlaylistCompareResponse;
import com.nos.movieuniverse.dto.PlaylistItemResponse;
import com.nos.movieuniverse.dto.PlaylistResponse;
import com.nos.movieuniverse.dto.StarredIdsResponse;
import com.nos.movieuniverse.dto.UserRatingResponse;
import com.nos.movieuniverse.model.Movie;
import com.nos.movieuniverse.model.Playlist;
import com.nos.movieuniverse.model.PlaylistItem;
import com.nos.movieuniverse.model.AppUser;
import com.nos.movieuniverse.model.UserRating;
import com.nos.movieuniverse.repository.AppUserRepository;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.repository.PlaylistItemRepository;
import com.nos.movieuniverse.repository.PlaylistRepository;
import com.nos.movieuniverse.repository.UserRatingRepository;
import com.nos.movieuniverse.tmdb.MovieEnricher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CatalogueService {

    private static final String STARRED_PLAYLIST_NAME = "Starred picks";

    private final MovieRepository movieRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final UserRatingRepository userRatingRepository;
    private final AppUserRepository appUserRepository;
    private final MovieCatalogueMapper mapper;
    private final MovieEnricher movieEnricher;

    public CatalogueService(
            MovieRepository movieRepository,
            PlaylistRepository playlistRepository,
            PlaylistItemRepository playlistItemRepository,
            UserRatingRepository userRatingRepository,
            AppUserRepository appUserRepository,
            MovieCatalogueMapper mapper,
            MovieEnricher movieEnricher) {
        this.movieRepository = movieRepository;
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
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

    public PlaylistCompareResponse comparePlaylists(long leftId, long rightId) {
        Playlist left = playlistRepository
                .findByIdAndDeletedAtIsNull(leftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Left playlist not found"));
        Playlist right = playlistRepository
                .findByIdAndDeletedAtIsNull(rightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Right playlist not found"));

        Map<Long, LocalRating> localRatings = loadLocalRatings();
        List<MovieResponse> leftFilms = filmsForPlaylist(left.getId(), localRatings);
        List<MovieResponse> rightFilms = filmsForPlaylist(right.getId(), localRatings);

        AverageScore leftAvg = averageCombined(leftFilms);
        AverageScore rightAvg = averageCombined(rightFilms);

        Set<Long> rightIds =
                rightFilms.stream().map(MovieResponse::tmdbId).collect(Collectors.toCollection(HashSet::new));
        Set<Long> seenCommon = new HashSet<>();
        List<MovieResponse> common = new ArrayList<>();
        for (MovieResponse film : leftFilms) {
            if (rightIds.contains(film.tmdbId()) && seenCommon.add(film.tmdbId())) {
                common.add(film);
            }
        }

        String winner = resolveWinner(leftAvg.average(), rightAvg.average());

        return new PlaylistCompareResponse(
                new PlaylistCompareResponse.CompareSide(
                        left.getId(),
                        left.getName(),
                        left.getOwner().getUsername(),
                        leftFilms.size(),
                        leftAvg.average(),
                        leftAvg.scorableCount()),
                new PlaylistCompareResponse.CompareSide(
                        right.getId(),
                        right.getName(),
                        right.getOwner().getUsername(),
                        rightFilms.size(),
                        rightAvg.average(),
                        rightAvg.scorableCount()),
                winner,
                common.size(),
                common);
    }

    public StarredIdsResponse findStarredTmdbIds(String username) {
        AppUser user = requireUser(username);
        Optional<Playlist> starred = findStarredPlaylist(user);
        if (starred.isEmpty()) {
            return new StarredIdsResponse(List.of());
        }
        long playlistId = starred.get().getId();
        List<Long> ids = playlistItemRepository.findByPlaylistIdOrderByPositionAsc(playlistId).stream()
                .map(item -> item.getMovie().getTmdbId())
                .distinct()
                .toList();
        return new StarredIdsResponse(ids);
    }

    @Transactional
    public PlaylistItemResponse addPlaylistItem(String username, long playlistId, long tmdbId) {
        AppUser owner = requireUser(username);
        Playlist playlist = requireOwnedPlaylist(owner, playlistId);
        Movie movie = movieRepository
                .findById(tmdbId)
                .orElseGet(() -> movieRepository.save(new Movie(tmdbId)));
        int nextPosition = playlistItemRepository.findByPlaylistIdOrderByPositionAsc(playlistId).stream()
                        .mapToInt(PlaylistItem::getPosition)
                        .max()
                        .orElse(0)
                + 1;
        PlaylistItem saved = playlistItemRepository.save(new PlaylistItem(playlist, movie, nextPosition));
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return toItemResponse(saved, localRatings);
    }

    @Transactional
    public void removePlaylistItem(String username, long playlistId, long itemId) {
        AppUser owner = requireUser(username);
        requireOwnedPlaylist(owner, playlistId);
        PlaylistItem item = playlistItemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist item not found"));
        if (item.getPlaylist().getId() != playlistId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist item not found");
        }
        playlistItemRepository.delete(item);
    }

    @Transactional
    public PlaylistItemResponse movePlaylistItem(String username, long itemId, long targetPlaylistId) {
        AppUser owner = requireUser(username);
        PlaylistItem item = playlistItemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist item not found"));
        Playlist source = item.getPlaylist();
        if (!source.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your playlist");
        }
        if (source.getId() == targetPlaylistId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Film is already in that playlist");
        }
        Playlist target = requireOwnedPlaylist(owner, targetPlaylistId);
        Movie movie = item.getMovie();
        playlistItemRepository.delete(item);
        int nextPosition = playlistItemRepository.findByPlaylistIdOrderByPositionAsc(targetPlaylistId).stream()
                        .mapToInt(PlaylistItem::getPosition)
                        .max()
                        .orElse(0)
                + 1;
        PlaylistItem moved = playlistItemRepository.save(new PlaylistItem(target, movie, nextPosition));
        Map<Long, LocalRating> localRatings = loadLocalRatings();
        return toItemResponse(moved, localRatings);
    }

    @Transactional
    public void addStarredFilm(String username, long tmdbId) {
        AppUser user = requireUser(username);
        Playlist starred = ensureStarredPlaylist(user);
        List<PlaylistItem> existing =
                playlistItemRepository.findByPlaylistIdAndMovieTmdbId(starred.getId(), tmdbId);
        if (!existing.isEmpty()) {
            return;
        }
        addPlaylistItem(username, starred.getId(), tmdbId);
    }

    @Transactional
    public void removeStarredFilm(String username, long tmdbId) {
        AppUser user = requireUser(username);
        findStarredPlaylist(user).ifPresent(starred -> {
            List<PlaylistItem> matches = playlistItemRepository.findByPlaylistIdAndMovieTmdbId(starred.getId(), tmdbId);
            playlistItemRepository.deleteAll(matches);
        });
    }

    public UserRatingResponse findMyRating(String username, long tmdbId) {
        AppUser user = requireUser(username);
        return userRatingRepository
                .findByUserIdAndMovieTmdbId(user.getId(), tmdbId)
                .map(rating -> new UserRatingResponse(tmdbId, (int) rating.getStars()))
                .orElse(new UserRatingResponse(tmdbId, null));
    }

    @Transactional
    public UserRatingResponse upsertMyRating(String username, long tmdbId, short stars) {
        if (stars < 1 || stars > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 10");
        }
        AppUser user = requireUser(username);
        Movie movie = movieRepository
                .findById(tmdbId)
                .orElseGet(() -> movieRepository.save(new Movie(tmdbId)));

        UserRating rating = userRatingRepository
                .findByUserIdAndMovieTmdbId(user.getId(), tmdbId)
                .orElseGet(() -> new UserRating(user, movie, stars, LocalDate.now()));
        rating.setStars(stars);
        rating.setRatedAt(LocalDate.now());
        userRatingRepository.save(rating);
        return new UserRatingResponse(tmdbId, (int) stars);
    }

    private AppUser requireUser(String username) {
        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private List<MovieResponse> filmsForPlaylist(long playlistId, Map<Long, LocalRating> localRatings) {
        return entriesForPlaylist(playlistId, localRatings).stream()
                .map(PlaylistItemResponse::film)
                .toList();
    }

    private List<PlaylistItemResponse> entriesForPlaylist(long playlistId, Map<Long, LocalRating> localRatings) {
        return playlistItemRepository.findByPlaylistIdOrderByPositionAsc(playlistId).stream()
                .map(item -> toItemResponse(item, localRatings))
                .toList();
    }

    private PlaylistItemResponse toItemResponse(PlaylistItem item, Map<Long, LocalRating> localRatings) {
        Movie movie = movieRepository
                .findById(item.getMovie().getTmdbId())
                .orElse(item.getMovie());
        return new PlaylistItemResponse(
                item.getId(), item.getPosition(), toSummary(movie, localRatings));
    }

    private Playlist requireOwnedPlaylist(AppUser owner, long playlistId) {
        Playlist playlist = playlistRepository
                .findByIdAndDeletedAtIsNull(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        if (!playlist.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your playlist");
        }
        return playlist;
    }

    private PlaylistResponse toResponse(Playlist playlist, Map<Long, LocalRating> localRatings) {
        List<PlaylistItemResponse> entries = entriesForPlaylist(playlist.getId(), localRatings);

        return new PlaylistResponse(
                playlist.getId(),
                playlist.getExternalId(),
                playlist.getName(),
                playlist.getOwner().getUsername(),
                entries.size(),
                entries);
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

    private Playlist ensureStarredPlaylist(AppUser user) {
        return findStarredPlaylist(user)
                .orElseGet(() -> {
                    Playlist playlist = new Playlist(STARRED_PLAYLIST_NAME, user);
                    playlist.setExternalId(starredExternalId(user.getUsername()));
                    return playlistRepository.save(playlist);
                });
    }

    private Optional<Playlist> findStarredPlaylist(AppUser user) {
        return playlistRepository.findByOwnerIdAndExternalIdAndDeletedAtIsNull(
                user.getId(), starredExternalId(user.getUsername()));
    }

    private static String starredExternalId(String username) {
        return "starred-" + username;
    }

    private static AverageScore averageCombined(List<MovieResponse> films) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (MovieResponse film : films) {
            if (film.combinedRating() != null) {
                sum = sum.add(film.combinedRating());
                count++;
            }
        }
        if (count == 0) {
            return new AverageScore(null, 0);
        }
        return new AverageScore(sum.divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP), count);
    }

    private static String resolveWinner(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return "insufficient";
        }
        if (left == null) {
            return "right";
        }
        if (right == null) {
            return "left";
        }
        int cmp = left.compareTo(right);
        if (cmp > 0) {
            return "left";
        }
        if (cmp < 0) {
            return "right";
        }
        return "tie";
    }

    private record LocalRating(long voteCount, BigDecimal average) {
        private static final LocalRating NONE = new LocalRating(0, null);
    }

    private record AverageScore(BigDecimal average, int scorableCount) {}
}
