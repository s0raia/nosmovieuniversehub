package com.nos.movieuniverse.seed.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Parses the real, unmodified seed file rather than a fixture.
 *
 * <p>Beyond checking that the DTOs bind, this pins down the four awkward things
 * in the data that the schema was designed around. If a later refactor quietly
 * breaks one of them, a test fails here rather than at import time.
 *
     * <p>The mapper is deliberately left strict: a plain {@link ObjectMapper} fails
     * on unknown properties, so this also proves every key in the file is mapped
     * and nothing is being silently dropped.
     *
     * <p>Note the import: Spring Boot 4 ships Jackson 3, which moved databind to
     * {@code tools.jackson.databind} while leaving the annotations behind in
     * {@code com.fasterxml.jackson.annotation}. Java time support is built in, so
     * no module has to be registered for {@link LocalDate}.
     */
class SeedFileParsingTest {

    private static SeedFile seed;

    @BeforeAll
    static void parseSeedFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in =
                SeedFileParsingTest.class.getResourceAsStream("/data/seed_playlists.json")) {
            assertThat(in)
                    .describedAs("seed_playlists.json must be on the classpath; the "
                            + "maven-resources-plugin copies it from the repo-root data/ folder")
                    .isNotNull();
            seed = mapper.readValue(in, SeedFile.class);
        }
    }

    @Test
    @DisplayName("every Portuguese key maps onto an English field")
    void bindsWholeFile() {
        assertThat(seed.version()).isNotBlank();
        assertThat(seed.description()).isNotBlank();
        assertThat(seed.users()).extracting(SeedUser::username).containsExactly("ana", "bruno", "carla");
        assertThat(seed.playlists()).hasSize(10);
        assertThat(seed.ratings()).hasSize(20);
    }

    @Test
    @DisplayName("trap 1: pl-01 holds the same film at two positions")
    void sameFilmAppearsTwiceInOnePlaylist() {
        List<SeedPlaylistItem> items = playlist("pl-01").items();

        assertThat(items)
                .filteredOn(item -> item.tmdbId() == 27205L)
                .extracting(SeedPlaylistItem::position)
                .containsExactlyInAnyOrder(1, 6);

        // Which is why (playlist, film) cannot be a key, but (playlist, position) can.
        assertThat(items).extracting(SeedPlaylistItem::position).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("trap 2: deleted playlists hold films found nowhere else")
    void deletedPlaylistsStillCarryUniqueFilms() {
        assertThat(seed.playlists())
                .filteredOn(SeedPlaylist::deleted)
                .extracting(SeedPlaylist::externalId)
                .containsExactlyInAnyOrder("pl-03", "pl-07");

        List<Long> onlyInLivePlaylists = seed.playlists().stream()
                .filter(playlist -> !playlist.deleted())
                .flatMap(playlist -> playlist.items().stream())
                .map(SeedPlaylistItem::tmdbId)
                .distinct()
                .toList();

        // Soft delete, not a skipped import: drop these rows and the films vanish.
        assertThat(onlyInLivePlaylists).doesNotContain(550L, 807L, 348L, 289L);
    }

    @Test
    @DisplayName("trap 3: a title identifies nothing")
    void distinctFilmsShareTitles() {
        List<Long> allFilmIds = seed.playlists().stream()
                .flatMap(playlist -> playlist.items().stream())
                .map(SeedPlaylistItem::tmdbId)
                .distinct()
                .toList();

        assertThat(allFilmIds).hasSize(34);
        assertThat(allFilmIds).contains(841L, 438631L); // two films called Dune
        assertThat(allFilmIds).contains(8587L, 420818L); // two called The Lion King
    }

    @Test
    @DisplayName("trap 4: every row has a natural key a re-import can match on")
    void naturalKeysAreUnique() {
        assertThat(seed.playlists())
                .extracting(SeedPlaylist::externalId)
                .doesNotHaveDuplicates()
                .allSatisfy(id -> assertThat(id).isNotBlank());

        assertThat(seed.ratings())
                .extracting(rating -> rating.username() + ":" + rating.tmdbId())
                .doesNotHaveDuplicates();

        Map<String, Long> playlistsPerOwner = seed.playlists().stream()
                .collect(Collectors.groupingBy(SeedPlaylist::ownerUsername, Collectors.counting()));
        assertThat(playlistsPerOwner).containsOnlyKeys("ana", "bruno", "carla");
    }

    @Test
    @DisplayName("ratings parse into real dates and stay within 1 to 10")
    void ratingsAreWellFormed() {
        assertThat(seed.ratings()).allSatisfy(rating -> {
            assertThat(rating.stars()).isBetween((short) 1, (short) 10);
            assertThat(rating.ratedOn()).isNotNull().isAfter(LocalDate.of(2000, 1, 1));
            assertThat(rating.username()).isNotBlank();
            assertThat(rating.tmdbId()).isNotNull();
        });
    }

    @Test
    @DisplayName("positions are one-based and contiguous within each playlist")
    void positionsStartAtOne() {
        assertThat(seed.playlists()).allSatisfy(playlist -> {
            List<Integer> positions =
                    playlist.items().stream().map(SeedPlaylistItem::position).sorted().toList();
            assertThat(positions.getFirst()).isEqualTo(1);
            assertThat(positions.getLast()).isEqualTo(positions.size());
        });
    }

    private static SeedPlaylist playlist(String externalId) {
        Map<String, SeedPlaylist> byId = seed.playlists().stream()
                .collect(Collectors.toMap(SeedPlaylist::externalId, Function.identity()));
        return byId.get(externalId);
    }
}
