package com.nos.movieuniverse.api;

import com.nos.movieuniverse.api.dto.MovieDetailResponse;
import com.nos.movieuniverse.api.dto.MovieResponse;
import com.nos.movieuniverse.domain.Movie;
import com.nos.movieuniverse.rating.CombinedRatingCalculator;
import com.nos.movieuniverse.rating.RatingInput;
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
                CombinedRatingCalculator.combine(input).orElse(null));
    }

    public MovieDetailResponse toDetail(Movie movie, BigDecimal localAverage, long localVoteCount) {
        MovieResponse summary = toSummary(movie, localAverage, localVoteCount);
        List<String> genres = movie.getGenres().stream().map(g -> g.getName()).sorted().toList();

        return new MovieDetailResponse(
                summary.tmdbId(),
                summary.title(),
                movie.getOriginalTitle(),
                summary.releaseYear(),
                summary.posterUrl(),
                summary.overview(),
                movie.getRuntime(),
                genres,
                summary.tmdbVoteAverage(),
                summary.tmdbVoteCount(),
                summary.localVoteAverage(),
                summary.localVoteCount(),
                summary.combinedRating());
    }
}
