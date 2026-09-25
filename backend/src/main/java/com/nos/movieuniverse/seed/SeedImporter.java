package com.nos.movieuniverse.seed;

import com.nos.movieuniverse.domain.AppUser;
import com.nos.movieuniverse.domain.Movie;
import com.nos.movieuniverse.domain.Playlist;
import com.nos.movieuniverse.domain.PlaylistItem;
import com.nos.movieuniverse.domain.UserRating;
import com.nos.movieuniverse.repository.AppUserRepository;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.repository.PlaylistItemRepository;
import com.nos.movieuniverse.repository.PlaylistRepository;
import com.nos.movieuniverse.repository.UserRatingRepository;
import com.nos.movieuniverse.seed.dto.SeedFile;
import com.nos.movieuniverse.seed.dto.SeedPlaylist;
import com.nos.movieuniverse.seed.dto.SeedPlaylistItem;
import com.nos.movieuniverse.seed.dto.SeedRating;
import com.nos.movieuniverse.seed.dto.SeedUser;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads a seed file into the database, and can do so repeatedly without
 * duplicating anything.
 *
 * <p>Every row is matched on a natural key rather than on a generated id:
 * username for users, the file's own id for playlists, (playlist, position) for
 * playlist entries, and (user, film) for ratings. Re-running an unchanged file
 * therefore reports no changes at all.
 *
 * <p>Films are inserted as stubs carrying only their TMDB id. The seed has no
 * titles in it, and foreign keys have to be satisfiable before anything has
 * talked to TMDB.
 */
@Service
public class SeedImporter {

    /**
     * Used when {@code SEED_DEFAULT_PASSWORD} is blank. The seed file contains no
     * credentials, so a reviewer who copied {@code .env.example} and filled in
     * only their TMDB token still needs to be able to log in as ana. Published
     * in the README for exactly that reason.
     */
    public static final String FALLBACK_PASSWORD = "movieuniverse";

    private static final Logger log = LoggerFactory.getLogger(SeedImporter.class);

    /**
     * Built here rather than injected. The application-wide mapper is lenient
     * about unknown properties, which is right for HTTP payloads and wrong for a
     * seed file: an unrecognised key means the file is not what this code thinks
     * it is, and silently importing part of it is the worst outcome.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    private final AppUserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final UserRatingRepository userRatingRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties properties;
    private final ResourceLoader resourceLoader;

    public SeedImporter(
            AppUserRepository userRepository,
            MovieRepository movieRepository,
            PlaylistRepository playlistRepository,
            PlaylistItemRepository playlistItemRepository,
            UserRatingRepository userRatingRepository,
            PasswordEncoder passwordEncoder,
            SeedProperties properties,
            ResourceLoader resourceLoader) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
        this.userRatingRepository = userRatingRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    /** Imports one seed file, given any location Spring can resolve. */
    @Transactional
    public SeedImportSummary importFrom(String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalArgumentException("Seed file not found: " + location);
        }

        SeedFile seed = read(resource, location);
        Counters counters = new Counters();

        Map<String, AppUser> usersByName = importUsers(seed, counters);
        Map<Long, Movie> filmsById = importFilmStubs(seed, counters);
        importPlaylists(seed, usersByName, filmsById, counters);
        importRatings(seed, usersByName, filmsById, counters);

        return counters.toSummary();
    }

    private SeedFile read(Resource resource, String location) {
        try (InputStream in = resource.getInputStream()) {
            return mapper.readValue(in, SeedFile.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read seed file: " + location, e);
        }
    }

    private Map<String, AppUser> importUsers(SeedFile seed, Counters counters) {
        String rawPassword = resolvePassword();
        Map<String, AppUser> byName = new HashMap<>();

        for (SeedUser seedUser : seed.users()) {
            // An existing user keeps their current hash. Re-encoding would change
            // it on every import and invalidate a password the reviewer had changed.
            AppUser user = userRepository
                    .findByUsername(seedUser.username())
                    .orElseGet(() -> {
                        counters.usersCreated++;
                        return userRepository.save(
                                new AppUser(seedUser.username(), passwordEncoder.encode(rawPassword)));
                    });
            byName.put(seedUser.username(), user);
        }
        return byName;
    }

    private String resolvePassword() {
        String configured = properties.defaultPassword();
        if (configured == null || configured.isBlank()) {
            log.warn(
                    "SEED_DEFAULT_PASSWORD is not set, so seeded users get the published "
                            + "fallback password \"{}\". Set it in .env to use your own.",
                    FALLBACK_PASSWORD);
            return FALLBACK_PASSWORD;
        }
        return configured;
    }

    /**
     * Creates a placeholder row for every film the file mentions, from playlists
     * and ratings alike, so the foreign keys below have something to point at.
     */
    private Map<Long, Movie> importFilmStubs(SeedFile seed, Counters counters) {
        Set<Long> tmdbIds = new LinkedHashSet<>();
        seed.playlists().forEach(playlist ->
                playlist.items().forEach(item -> tmdbIds.add(item.tmdbId())));
        seed.ratings().forEach(rating -> tmdbIds.add(rating.tmdbId()));

        Map<Long, Movie> byId = new HashMap<>();
        movieRepository.findAllById(tmdbIds).forEach(movie -> byId.put(movie.getTmdbId(), movie));

        for (Long tmdbId : tmdbIds) {
            if (!byId.containsKey(tmdbId)) {
                // A stub is valid on purpose: no votes, so a null average, which is
                // exactly what the vote constraint requires.
                byId.put(tmdbId, movieRepository.save(new Movie(tmdbId)));
                counters.movieStubsCreated++;
            }
        }
        return byId;
    }

    private void importPlaylists(
            SeedFile seed, Map<String, AppUser> usersByName, Map<Long, Movie> filmsById, Counters counters) {

        for (SeedPlaylist seedPlaylist : seed.playlists()) {
            Optional<Playlist> existing = playlistRepository.findByExternalId(seedPlaylist.externalId());
            Playlist playlist;

            if (existing.isEmpty()) {
                playlist = new Playlist(seedPlaylist.name(), requireUser(usersByName, seedPlaylist.ownerUsername()));
                playlist.setExternalId(seedPlaylist.externalId());
                applyDeletedFlag(playlist, seedPlaylist.deleted());
                playlist = playlistRepository.save(playlist);
                counters.playlistsCreated++;
            } else {
                playlist = existing.get();
                boolean changed = false;
                if (!playlist.getName().equals(seedPlaylist.name())) {
                    playlist.setName(seedPlaylist.name());
                    changed = true;
                }
                if (playlist.isDeleted() != seedPlaylist.deleted()) {
                    applyDeletedFlag(playlist, seedPlaylist.deleted());
                    changed = true;
                }
                if (changed) {
                    playlistRepository.save(playlist);
                    counters.playlistsUpdated++;
                }
            }

            importPlaylistItems(playlist, seedPlaylist, filmsById, counters);
        }
    }

    private void applyDeletedFlag(Playlist playlist, boolean deleted) {
        // Soft delete: the two deleted playlists in the seed hold films that appear
        // nowhere else, so they are imported as rows and merely marked.
        playlist.setDeletedAt(deleted ? OffsetDateTime.now() : null);
    }

    private void importPlaylistItems(
            Playlist playlist, SeedPlaylist seedPlaylist, Map<Long, Movie> filmsById, Counters counters) {

        for (SeedPlaylistItem seedItem : seedPlaylist.items()) {
            // Keyed on position, never on the film: pl-01 holds the same film twice.
            Optional<PlaylistItem> existing =
                    playlistItemRepository.findByPlaylistIdAndPosition(playlist.getId(), seedItem.position());

            if (existing.isEmpty()) {
                playlistItemRepository.save(
                        new PlaylistItem(playlist, filmsById.get(seedItem.tmdbId()), seedItem.position()));
                counters.itemsCreated++;
            } else if (!existing.get().getMovie().getTmdbId().equals(seedItem.tmdbId())) {
                // That slot now holds a different film. Replace rather than mutate, so
                // the surrogate id always identifies one film at one position.
                playlistItemRepository.delete(existing.get());
                playlistItemRepository.save(
                        new PlaylistItem(playlist, filmsById.get(seedItem.tmdbId()), seedItem.position()));
                counters.itemsUpdated++;
            }
        }
    }

    private void importRatings(
            SeedFile seed, Map<String, AppUser> usersByName, Map<Long, Movie> filmsById, Counters counters) {

        for (SeedRating seedRating : seed.ratings()) {
            AppUser user = requireUser(usersByName, seedRating.username());
            Optional<UserRating> existing =
                    userRatingRepository.findByUserIdAndMovieTmdbId(user.getId(), seedRating.tmdbId());

            if (existing.isEmpty()) {
                userRatingRepository.save(new UserRating(
                        user, filmsById.get(seedRating.tmdbId()), seedRating.stars(), seedRating.ratedOn()));
                counters.ratingsCreated++;
                continue;
            }

            UserRating rating = existing.get();
            boolean changed = false;
            if (rating.getStars() != seedRating.stars()) {
                rating.setStars(seedRating.stars());
                changed = true;
            }
            if (!rating.getRatedAt().equals(seedRating.ratedOn())) {
                rating.setRatedAt(seedRating.ratedOn());
                changed = true;
            }
            if (changed) {
                userRatingRepository.save(rating);
                counters.ratingsUpdated++;
            }
        }
    }

    private AppUser requireUser(Map<String, AppUser> usersByName, String username) {
        AppUser user = usersByName.get(username);
        if (user == null) {
            throw new IllegalStateException(
                    "Seed file references user \"" + username + "\", who is not in its own user list");
        }
        return user;
    }

    private static final class Counters {
        private int usersCreated;
        private int movieStubsCreated;
        private int playlistsCreated;
        private int playlistsUpdated;
        private int itemsCreated;
        private int itemsUpdated;
        private int ratingsCreated;
        private int ratingsUpdated;

        private SeedImportSummary toSummary() {
            return new SeedImportSummary(
                    usersCreated,
                    movieStubsCreated,
                    playlistsCreated,
                    playlistsUpdated,
                    itemsCreated,
                    itemsUpdated,
                    ratingsCreated,
                    ratingsUpdated);
        }
    }
}
