package com.nos.movieuniverse.seed.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class MockExtraParsingTest {

    private static SeedFile mockExtra;

    @BeforeAll
    static void parseMockExtra() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = MockExtraParsingTest.class.getResourceAsStream("/data/mock_extra.json")) {
            mockExtra = mapper.readValue(in, SeedFile.class);
        }
    }

    @Test
    void bindsDisplayNamesAndSoraiaUser() {
        assertThat(mockExtra.users()).hasSize(21);
        assertThat(mockExtra.users().stream().filter(u -> "soraia".equals(u.username())).findFirst())
                .hasValueSatisfying(u -> assertThat(u.displayName()).isEqualTo("Soraia"));
        assertThat(mockExtra.users().stream().filter(u -> "mock-06".equals(u.username())).findFirst())
                .hasValueSatisfying(u -> assertThat(u.displayName()).isEqualTo("Yuki"));
    }

    @Test
    void bindsPersonaDemoUsers() {
        assertThat(mockExtra.users().stream().filter(u -> "joao".equals(u.username())).findFirst())
                .hasValueSatisfying(u -> assertThat(u.displayName()).isEqualTo("João"));
        assertThat(mockExtra.playlists().stream().anyMatch(p -> "mock-pl-sabrina-01".equals(p.externalId())))
                .isTrue();
        assertThat(mockExtra.playlists().stream().anyMatch(p -> "mock-pl-andres-01".equals(p.externalId())))
                .isTrue();
    }

    @Test
    void includesSoraiaPlaylistsFromTrakt() {
        assertThat(mockExtra.playlists().stream().anyMatch(p -> "mock-pl-soraia-01".equals(p.externalId())))
                .isTrue();
        assertThat(mockExtra.ratings().stream().filter(r -> "soraia".equals(r.username())).count())
                .isGreaterThanOrEqualTo(20);
    }
}
