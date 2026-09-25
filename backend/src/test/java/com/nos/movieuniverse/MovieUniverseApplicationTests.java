package com.nos.movieuniverse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Boots the whole application against a throwaway Postgres, which is what makes
 * this worth having: it proves Flyway applies the baseline and that every
 * entity passes {@code ddl-auto: validate} from an empty database.
 *
 * <p>Both startup runners are switched off. Seeding is not what this test is
 * about, and enrichment would make real TMDB calls, which would make the build
 * slow, network-dependent and dependent on a token no CI machine has.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(
		properties = {
			"app.seed.auto-on-empty=false",
			"tmdb.enrich-stubs-on-startup=false",
			"tmdb.read-access-token=not-used-in-this-test"
		})
class MovieUniverseApplicationTests {

	@Test
	void contextLoads() {
	}

}
