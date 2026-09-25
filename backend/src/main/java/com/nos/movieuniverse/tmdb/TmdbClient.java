package com.nos.movieuniverse.tmdb;

import com.nos.movieuniverse.tmdb.dto.TmdbMovie;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Reads films from TMDB.
 *
 * <p>Authentication uses the v4 Read Access Token as a bearer header. The token
 * is injected from the environment and is never logged: failures below report
 * the status and the film id only, because a request dump would carry the
 * Authorization header with it.
 */
@Component
public class TmdbClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);

    private final RestClient restClient;
    private final TmdbProperties properties;

    public TmdbClient(TmdbProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.readAccessToken())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    /**
     * Fetches one film.
     *
     * @return empty when TMDB does not know the id, or when the call fails. A
     *     missing film is not an error: the stub row simply stays a stub and the
     *     next run tries again.
     */
    public Optional<TmdbMovie> fetchMovie(long tmdbId) {
        if (!properties.isConfigured()) {
            log.warn("TMDB_API_READ_TOKEN is not set, so film {} cannot be fetched", tmdbId);
            return Optional.empty();
        }

        try {
            TmdbMovie movie = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("language", properties.language())
                            .build(tmdbId))
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            (request, response) -> {
                                throw new TmdbNotFoundException(tmdbId);
                            })
                    .body(TmdbMovie.class);
            return Optional.ofNullable(movie);
        } catch (TmdbNotFoundException e) {
            log.info("TMDB has no film with id {}", tmdbId);
            return Optional.empty();
        } catch (RestClientException e) {
            // Deliberately terse. The exception's message can include the request
            // line, and anything more detailed risks putting the token in a log.
            log.warn("Could not fetch film {} from TMDB: {}", tmdbId, e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    /** Signals a 404 from inside the status handler, where returning is not possible. */
    static class TmdbNotFoundException extends RuntimeException {
        TmdbNotFoundException(long tmdbId) {
            super("No TMDB film with id " + tmdbId);
        }
    }
}
