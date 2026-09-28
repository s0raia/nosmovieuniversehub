package com.nos.movieuniverse.tmdb;

import com.nos.movieuniverse.tmdb.dto.TmdbMovie;
import com.nos.movieuniverse.tmdb.dto.TmdbTrendingResponse;
import java.time.LocalDate;
import java.util.List;
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

    /** TMDB trending movies this week (global list, not limited to the seed catalogue). */
    public List<Long> fetchTrendingMovieIds(int limit) {
        return fetchResultIds("/trending/movie/week", limit, uriBuilder -> uriBuilder
                .queryParam("language", properties.language()));
    }

    /** Films with a primary release date in the last six months, newest first. */
    public List<Long> fetchLatestReleaseMovieIds(int limit) {
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6);
        return fetchResultIds("/discover/movie", limit, uriBuilder -> uriBuilder
                .queryParam("language", properties.language())
                .queryParam("sort_by", "release_date.desc")
                .queryParam("primary_release_date.gte", sixMonthsAgo.toString())
                .queryParam("primary_release_date.lte", today.toString())
                .queryParam("include_adult", false));
    }

    /** Upcoming wide releases from TMDB. */
    public List<Long> fetchUpcomingMovieIds(int limit) {
        return fetchResultIds("/movie/upcoming", limit, uriBuilder -> uriBuilder
                .queryParam("language", properties.language())
                .queryParam("region", "US"));
    }

    private List<Long> fetchResultIds(
            String path, int limit, java.util.function.Consumer<
                            org.springframework.web.util.UriBuilder>
                    extraParams) {
        if (!properties.isConfigured()) {
            return List.of();
        }
        try {
            TmdbTrendingResponse body = restClient
                    .get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(path);
                        extraParams.accept(builder);
                        return builder.build();
                    })
                    .retrieve()
                    .body(TmdbTrendingResponse.class);
            if (body == null || body.results() == null) {
                return List.of();
            }
            return body.results().stream()
                    .map(TmdbTrendingResponse.TmdbTrendingItem::id)
                    .filter(id -> id != null)
                    .limit(limit)
                    .toList();
        } catch (RestClientException e) {
            log.warn("Could not fetch TMDB list at {}: {}", path, e.getClass().getSimpleName());
            return List.of();
        }
    }

    /** Signals a 404 from inside the status handler, where returning is not possible. */
    static class TmdbNotFoundException extends RuntimeException {
        TmdbNotFoundException(long tmdbId) {
            super("No TMDB film with id " + tmdbId);
        }
    }
}
