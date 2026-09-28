package com.nos.movieuniverse.seed;

import com.nos.movieuniverse.repository.AppUserRepository;
import java.util.List;
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

    /** Added after initial mock import; re-run mock_extra when any of these are absent. */
    private static final List<String> LATER_MOCK_USERS = List.of("soraia", "joao", "sabrina", "andres");

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
        boolean needsInitialMock = !userRepository.existsByUsername(MARKER_USER);
        boolean needsLaterMockUsers =
                LATER_MOCK_USERS.stream().anyMatch(username -> !userRepository.existsByUsername(username));
        boolean needsDisplayNames = userRepository
                .findByUsername(MARKER_USER)
                .map(user -> user.getDisplayName() == null || user.getDisplayName().isBlank())
                .orElse(false);
        if (!needsInitialMock && !needsLaterMockUsers && !needsDisplayNames) {
            log.debug("Mock data up to date, skipping mock_extra import");
            return;
        }
        log.info("Importing mock users and themed playlists from classpath:data/mock_extra.json");
        SeedImportSummary summary = importer.importFrom("classpath:data/mock_extra.json");
        log.info("Mock import finished: {}", summary);
    }
}
