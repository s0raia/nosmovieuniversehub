package com.nos.movieuniverse.seed;

import com.nos.movieuniverse.repository.AppUserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Decides whether to import seed data at startup.
 *
 * <p>Two ways in. {@code --import=<location>} always runs and can be repeated,
 * which is how extra mock data gets loaded on top of an existing database.
 * Otherwise the configured files are imported only when the database holds no
 * users, so an ordinary restart leaves a reviewer's work alone.
 */
@Component
@Order(10)
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final SeedImporter importer;
    private final SeedProperties properties;
    private final AppUserRepository userRepository;

    public SeedRunner(SeedImporter importer, SeedProperties properties, AppUserRepository userRepository) {
        this.importer = importer;
        this.properties = properties;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> requested = args.getOptionValues("import");
        if (requested != null && !requested.isEmpty()) {
            requested.forEach(this::runImport);
            return;
        }

        if (!properties.autoOnEmpty()) {
            log.debug("Seed import on empty database is disabled");
            return;
        }

        long existingUsers = userRepository.count();
        if (existingUsers > 0) {
            // Importing unconditionally would overwrite edits made through the UI,
            // which is why this is not simply "import every boot".
            log.info("Skipping seed import: the database already holds {} user(s)", existingUsers);
            return;
        }

        if (properties.files().isEmpty()) {
            log.warn("Database is empty but no seed files are configured under app.seed.files");
            return;
        }

        properties.files().forEach(this::runImport);
    }

    private void runImport(String location) {
        log.info("Importing seed data from {}", location);
        SeedImportSummary summary = importer.importFrom(location);
        log.info("Seed import finished: {}", summary);
    }
}
