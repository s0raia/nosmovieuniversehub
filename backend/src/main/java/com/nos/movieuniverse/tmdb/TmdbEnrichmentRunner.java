package com.nos.movieuniverse.tmdb;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Fills in film details after the seed importer has run.
 *
 * <p>Ordered behind the importer, which is what creates the stubs this reads.
 * Idempotent by construction: a film that has been fetched is no longer a stub,
 * so a restart finds nothing to do.
 */
@Component
@Order(20)
public class TmdbEnrichmentRunner implements ApplicationRunner {

    private final MovieEnrichmentService enrichmentService;
    private final TmdbProperties properties;

    public TmdbEnrichmentRunner(MovieEnrichmentService enrichmentService, TmdbProperties properties) {
        this.enrichmentService = enrichmentService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enrichStubsOnStartup() || !properties.isConfigured()) {
            return;
        }
        enrichmentService.enrichAllStubs();
    }
}
