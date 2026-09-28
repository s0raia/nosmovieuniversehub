package com.nos.movieuniverse.service;

import com.nos.movieuniverse.dto.MovieDetailResponse;
import com.nos.movieuniverse.dto.MovieResponse;
import com.nos.movieuniverse.model.Movie;
import com.nos.movieuniverse.service.rating.CombinedRatingCalculator;
import com.nos.movieuniverse.service.rating.RatingInput;
import com.nos.movieuniverse.tmdb.TmdbProperties;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MovieCatalogueMapper {

    private final TmdbProperties tmdbProperties;

    public MovieCatalogueMapper(TmdbProperties tmdbProperties) {
        this.tmdbProperties = tmdbProperties;
    }

    public MovieResponse toSummary(Movie movie, BigDecimal localAverage, long localVoteCount) {
        RatingInput input = new RatingInput(
                movie.getTmdbVoteCount(), movie.getTmdbVoteAverage(), (int) localVoteCount, localAverage);

        List<String> genres =
                movie.getGenres().stream().map(g -> g.getName()).sorted().toList();

        return new MovieResponse(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate() == null ? null : movie.getReleaseDate().getYear(),
                tmdbProperties.posterUrl(movie.getPosterPath()),
                movie.getOverview(),
                movie.getTmdbVoteAverage(),
                movie.getTmdbVoteCount(),
                localAverage,
                localVoteCount,
                CombinedRatingCalculator.combine(input).orElse(null),
                genres);
    }

    public MovieDetailResponse toDetail(Movie movie, BigDecimal localAverage, long localVoteCount) {
        MovieResponse summary = toSummary(movie, localAverage, localVoteCount);
        List<String> genres = movie.getGenres().stream().map(g -> g.getName()).sorted().toList();

        List<String> spokenLanguages =
                movie.getSpokenLanguages() == null ? List.of() : List.copyOf(movie.getSpokenLanguages());

        return new MovieDetailResponse(
                summary.tmdbId(),
                summary.title(),
                movie.getOriginalTitle(),
                summary.releaseYear(),
                summary.posterUrl(),
                summary.overview(),
                movie.getRuntime(),
                genres,
                movie.getOriginalLanguage(),
                spokenLanguages,
                summary.tmdbVoteAverage(),
                summary.tmdbVoteCount(),
                summary.localVoteAverage(),
                summary.localVoteCount(),
                summary.combinedRating());
    }
}
