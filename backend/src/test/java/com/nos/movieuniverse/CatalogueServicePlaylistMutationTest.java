package com.nos.movieuniverse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nos.movieuniverse.service.CatalogueService;
import com.nos.movieuniverse.model.AppUser;
import com.nos.movieuniverse.model.Movie;
import com.nos.movieuniverse.model.Playlist;
import com.nos.movieuniverse.model.PlaylistItem;
import com.nos.movieuniverse.repository.AppUserRepository;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.repository.PlaylistItemRepository;
import com.nos.movieuniverse.repository.PlaylistRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(
        properties = {
            "app.seed.auto-on-empty=false",
            "app.seed.import-mock-when-missing=false",
            "app.tmdb.backfill-missing-genres=false",
            "tmdb.enrich-stubs-on-startup=false",
            "tmdb.read-access-token=not-used-in-this-test"
        })
@Import(TestcontainersConfiguration.class)
@Transactional
class CatalogueServicePlaylistMutationTest {

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistItemRepository playlistItemRepository;

    @BeforeEach
    void seedUsers() {
        userRepository.save(new AppUser("owner-a", passwordEncoder.encode("secret")));
        userRepository.save(new AppUser("owner-b", passwordEncoder.encode("secret")));
    }

    @Test
    void ownerCanCreateAndRenamePlaylist() {
        var created = catalogueService.createPlaylist("owner-a", "Sunday sci-fi");
        assertThat(created.name()).isEqualTo("Sunday sci-fi");
        assertThat(created.externalId()).isNull();
        assertThat(created.filmCount()).isZero();

        var renamed = catalogueService.renamePlaylist("owner-a", created.id(), "Sunday Sci-Fi Marathon");
        assertThat(renamed.name()).isEqualTo("Sunday Sci-Fi Marathon");
    }

    @Test
    void createAcceptsUnicodeName() {
        var created = catalogueService.createPlaylist("owner-a", "Café 🎬");
        assertThat(created.name()).isEqualTo("Café 🎬");
    }

    @Test
    void otherUserCannotRenamePlaylist() {
        var created = catalogueService.createPlaylist("owner-a", "Private list");

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> catalogueService.renamePlaylist("owner-b", created.id(), "Taken"));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void starAddAndRemovePersistsInDatabase() {
        movieRepository.save(new Movie(999_001L));

        catalogueService.addStarredFilm("owner-a", 999_001L);
        assertThat(catalogueService.findStarredTmdbIds("owner-a").tmdbIds()).containsExactly(999_001L);

        catalogueService.removeStarredFilm("owner-a", 999_001L);
        assertThat(catalogueService.findStarredTmdbIds("owner-a").tmdbIds()).isEmpty();
    }

    @Test
    void ownerCanAddAndRemovePlaylistItem() {
        movieRepository.save(new Movie(999_010L));
        var playlist = catalogueService.createPlaylist("owner-a", "Weekend picks");

        var added = catalogueService.addPlaylistItem("owner-a", playlist.id(), 999_010L);
        assertThat(added.film().tmdbId()).isEqualTo(999_010L);

        var loaded = catalogueService.findPlaylistsForUser("owner-a");
        assertThat(loaded).hasSize(1);
        assertThat(loaded.getFirst().entries()).hasSize(1);
        assertThat(loaded.getFirst().entries().getFirst().itemId()).isEqualTo(added.itemId());

        catalogueService.removePlaylistItem("owner-a", playlist.id(), added.itemId());
        assertThat(catalogueService.findPlaylistsForUser("owner-a").getFirst().entries()).isEmpty();
    }

    @Test
    void otherUserCannotAddToPlaylist() {
        var playlist = catalogueService.createPlaylist("owner-a", "Private");
        movieRepository.save(new Movie(999_011L));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> catalogueService.addPlaylistItem("owner-b", playlist.id(), 999_011L));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void ownerCanMoveItemBetweenPlaylists() {
        movieRepository.save(new Movie(999_012L));
        var source = catalogueService.createPlaylist("owner-a", "Source");
        var target = catalogueService.createPlaylist("owner-a", "Target");
        var item = catalogueService.addPlaylistItem("owner-a", source.id(), 999_012L);

        var moved = catalogueService.movePlaylistItem("owner-a", item.itemId(), target.id());
        assertThat(moved.film().tmdbId()).isEqualTo(999_012L);
        assertThat(catalogueService.findPlaylistsForUser("owner-a").stream()
                        .filter(p -> p.id().equals(source.id()))
                        .findFirst()
                        .orElseThrow()
                        .entries())
                .isEmpty();
        assertThat(catalogueService.findPlaylistsForUser("owner-a").stream()
                        .filter(p -> p.id().equals(target.id()))
                        .findFirst()
                        .orElseThrow()
                        .entries())
                .hasSize(1);
    }

    @Test
    void cannotMoveItemToAnotherUsersPlaylist() {
        movieRepository.save(new Movie(999_013L));
        var source = catalogueService.createPlaylist("owner-a", "Mine");
        var target = catalogueService.createPlaylist("owner-b", "Theirs");
        var item = catalogueService.addPlaylistItem("owner-a", source.id(), 999_013L);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> catalogueService.movePlaylistItem("owner-a", item.itemId(), target.id()));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void userCanUpsertRating() {
        movieRepository.save(new Movie(999_002L));

        var first = catalogueService.upsertMyRating("owner-a", 999_002L, (short) 8);
        assertThat(first.stars()).isEqualTo(8);

        var updated = catalogueService.upsertMyRating("owner-a", 999_002L, (short) 10);
        assertThat(updated.stars()).isEqualTo(10);
        assertThat(catalogueService.findMyRating("owner-a", 999_002L).stars()).isEqualTo(10);
    }

    @Test
    void comparePlaylistsPicksHigherAverageCombinedRating() {
        Movie popular = movieRepository.save(stubMovie(100L, new BigDecimal("8.4"), 30_000));
        Movie obscure = movieRepository.save(stubMovie(200L, new BigDecimal("8.9"), 12));

        var leftDto = catalogueService.createPlaylist("owner-a", "Blockbusters");
        var rightDto = catalogueService.createPlaylist("owner-a", "Niche");

        Playlist left = playlistRepository.findById(leftDto.id()).orElseThrow();
        Playlist right = playlistRepository.findById(rightDto.id()).orElseThrow();
        playlistItemRepository.save(new PlaylistItem(left, popular, 1));
        playlistItemRepository.save(new PlaylistItem(right, obscure, 1));

        var compare = catalogueService.comparePlaylists(leftDto.id(), rightDto.id());
        assertThat(compare.winner()).isEqualTo("left");
        assertThat(compare.left().averageCombinedRating()).isGreaterThan(compare.right().averageCombinedRating());
    }

    private static Movie stubMovie(long id, BigDecimal avg, int votes) {
        Movie movie = new Movie(id);
        movie.setTitle("Film " + id);
        movie.setVotes(votes, avg);
        movie.setFetchedAt(OffsetDateTime.now());
        return movie;
    }
}
