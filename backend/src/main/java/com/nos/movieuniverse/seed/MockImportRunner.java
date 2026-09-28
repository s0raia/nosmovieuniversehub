package com.nos.movieuniverse.seed;

import com.nos.movieuniverse.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Loads {@code mock_extra.json} once, after the main seed, when mock users are
 * not present yet.
 */
@Component
@Order(15)
@ConditionalOnProperty(prefix = "app.seed", name = "import-mock-when-missing", havingValue = "true", matchIfMissing = true)
public class MockImportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MockImportRunner.class);
    private static final String MARKER_USER = "mock-01";

    private final SeedImporter importer;
    private final AppUserRepository userRepository;

    public MockImportRunner(SeedImporter importer, AppUserRepository userRepository) {
        this.importer = importer;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("import")) {
            return;
        }
        if (userRepository.existsByUsername(MARKER_USER)) {
            log.debug("Mock data already present, skipping mock_extra import");
            return;
        }
        log.info("Importing mock users and themed playlists from classpath:data/mock_extra.json");
        SeedImportSummary summary = importer.importFrom("classpath:data/mock_extra.json");
        log.info("Mock import finished: {}", summary);
    }
}
