package com.nos.movieuniverse.service;

import com.nos.movieuniverse.dto.HomeSectionResponse;
import com.nos.movieuniverse.dto.MovieResponse;
import com.nos.movieuniverse.model.Movie;
import com.nos.movieuniverse.repository.MovieRepository;
import com.nos.movieuniverse.repository.PlaylistItemRepository;
import com.nos.movieuniverse.repository.UserRatingRepository;
import com.nos.movieuniverse.tmdb.MovieEnricher;
import com.nos.movieuniverse.tmdb.TmdbClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeService {

    private static final int SECTION_SIZE = 8;

    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final MovieEnricher movieEnricher;
    private final MovieCatalogueMapper mapper;
    private final UserRatingRepository userRatingRepository;
    private final PlaylistItemRepository playlistItemRepository;

    public HomeService(
            TmdbClient tmdbClient,
            MovieRepository movieRepository,
            MovieEnricher movieEnricher,
            MovieCatalogueMapper mapper,
            UserRatingRepository userRatingRepository,
            PlaylistItemRepository playlistItemRepository) {
        this.tmdbClient = tmdbClient;
        this.movieRepository = movieRepository;
        this.movieEnricher = movieEnricher;
        this.mapper = mapper;
        this.userRatingRepository = userRatingRepository;
        this.playlistItemRepository = playlistItemRepository;
    }

    @Transactional
    public List<HomeSectionResponse> buildHomepage() {
        Map<Long, LocalRating> local = loadLocalRatings();
        List<HomeSectionResponse> sections = new ArrayList<>();

        sections.add(new HomeSectionResponse(
                "trending-week",
                "Popular this week",
                resolveMovies(tmdbClient.fetchTrendingMovieIds(SECTION_SIZE), local)));

        sections.add(new HomeSectionResponse(
                "latest-releases",
                "Latest releases",
                latestReleases(local)));

        sections.add(new HomeSectionResponse(
                "most-rewatched",
                "Most Rewatched This Year",
                mostRewatched(local)));

        sections.add(new HomeSectionResponse(
                "top-combined",
                "Top rated in MovieUniverse",
                topCombined(local)));

        sections.add(new HomeSectionResponse(
                "catalogue-spotlight",
                "Blockbusters in our library",
                blockbusters(local)));

        sections.add(new HomeSectionResponse(
                "anticipated",
                "Anticipated",
                resolveMovies(tmdbClient.fetchUpcomingMovieIds(SECTION_SIZE), local)));

        return sections;
    }

    private List<MovieResponse> latestReleases(Map<Long, LocalRating> local) {
        List<Long> fromTmdb = tmdbClient.fetchLatestReleaseMovieIds(SECTION_SIZE);
        if (!fromTmdb.isEmpty()) {
            return resolveMovies(fromTmdb, local);
        }

        LocalDate cutoff = LocalDate.now().minusMonths(6);
        return movieRepository.findAllWithGenres().stream()
                .filter(m -> m.getReleaseDate() != null && !m.getReleaseDate().isBefore(cutoff))
                .sorted((a, b) -> b.getReleaseDate().compareTo(a.getReleaseDate()))
                .limit(SECTION_SIZE)
                .map(m -> toSummary(m, local))
                .toList();
    }

    private List<MovieResponse> topCombined(Map<Long, LocalRating> local) {
        return movieRepository.findAllWithGenres().stream()
                .map(m -> toSummary(m, local))
                .sorted((a, b) -> Optional.ofNullable(b.combinedRating())
                        .orElse(BigDecimal.ZERO)
                        .compareTo(Optional.ofNullable(a.combinedRating()).orElse(BigDecimal.ZERO)))
                .limit(SECTION_SIZE)
                .toList();
    }

    private List<MovieResponse> blockbusters(Map<Long, LocalRating> local) {
        return movieRepository.findAllWithGenres().stream()
                .filter(m -> m.getTmdbVoteCount() > 10_000)
                .sorted((a, b) -> Integer.compare(b.getTmdbVoteCount(), a.getTmdbVoteCount()))
                .limit(SECTION_SIZE)
                .map(m -> toSummary(m, local))
                .toList();
    }

    private List<MovieResponse> mostRewatched(Map<Long, LocalRating> local) {
        return playlistItemRepository.countAppearancesByMovie().stream()
                .limit(SECTION_SIZE)
                .map(row -> movieRepository.findById(row.getTmdbId()))
                .flatMap(Optional::stream)
                .map(m -> toSummary(m, local))
                .toList();
    }

    private List<MovieResponse> resolveMovies(List<Long> tmdbIds, Map<Long, LocalRating> local) {
        List<MovieResponse> out = new ArrayList<>();
        for (Long id : tmdbIds) {
            Movie movie = movieRepository.findById(id).orElseGet(() -> movieRepository.save(new Movie(id)));
            if (movie.isStub()) {
                movieEnricher.enrich(id);
                movie = movieRepository.findById(id).orElse(movie);
            }
            out.add(toSummary(movie, local));
        }
        return out;
    }

    private MovieResponse toSummary(Movie movie, Map<Long, LocalRating> local) {
        LocalRating r = local.getOrDefault(movie.getTmdbId(), LocalRating.NONE);
        return mapper.toSummary(movie, r.average(), r.voteCount());
    }

    private Map<Long, LocalRating> loadLocalRatings() {
        Map<Long, LocalRating> byFilm = new HashMap<>();
        for (var aggregate : userRatingRepository.findLocalAggregates()) {
            BigDecimal average = aggregate.getAverage() == null
                    ? null
                    : BigDecimal.valueOf(aggregate.getAverage()).setScale(3, RoundingMode.HALF_UP);
            byFilm.put(aggregate.getTmdbId(), new LocalRating(aggregate.getVoteCount(), average));
        }
        return byFilm;
    }

    private record LocalRating(long voteCount, BigDecimal average) {
        private static final LocalRating NONE = new LocalRating(0, null);
    }
}
