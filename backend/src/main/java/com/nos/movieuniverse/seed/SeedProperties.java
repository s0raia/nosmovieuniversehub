package com.nos.movieuniverse.seed;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings for importing seed data.
 *
 * @param autoOnEmpty import the configured files at startup, but only when the
 *     database holds no users. Deliberately not "on every boot", which would
 *     undo a reviewer's changes each time the application restarted.
 * @param files resource locations, resolved through Spring's resource loader so
 *     both {@code classpath:} and {@code file:} work.
 * @param defaultPassword the password given to every seeded user. The seed file
 *     supplies no credentials at all, so one has to be invented and documented
 *     or nobody can log in as ana. Blank falls back to a published literal.
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
        @DefaultValue("true") boolean autoOnEmpty,
        @DefaultValue List<String> files,
        @DefaultValue("") String defaultPassword) {
}
