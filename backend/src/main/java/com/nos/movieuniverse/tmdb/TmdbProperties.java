package com.nos.movieuniverse.tmdb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * TMDB connection settings.
 *
 * @param readAccessToken the v4 API Read Access Token, sent as a bearer token.
 *     Not the v3 key, which goes in a query parameter instead. Supplied through
 *     the environment and never committed.
 * @param language requested explicitly so stored titles and overviews are
 *     English whatever the account default happens to be.
 * @param throttleMillis pause between requests. TMDB's published limit is far
 *     higher than anything this application does, but enriching the whole
 *     catalogue is the one place that could burst.
 * @param enrichStubsOnStartup fill in placeholder film rows at boot. Safe to
 *     leave on: once a film has been fetched it is no longer a stub, so this
 *     does nothing on subsequent restarts.
 */
@ConfigurationProperties(prefix = "tmdb")
public record TmdbProperties(
        @DefaultValue("https://api.themoviedb.org/3") String baseUrl,
        String readAccessToken,
        @DefaultValue("en-US") String language,
        @DefaultValue("60") long throttleMillis,
        @DefaultValue("true") boolean enrichStubsOnStartup) {

    public boolean isConfigured() {
        return readAccessToken != null && !readAccessToken.isBlank();
    }
}
