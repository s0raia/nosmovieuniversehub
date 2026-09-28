package com.nos.movieuniverse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nos.movieuniverse.service.CatalogueService;
import com.nos.movieuniverse.model.AppUser;
import com.nos.movieuniverse.repository.AppUserRepository;
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
}
