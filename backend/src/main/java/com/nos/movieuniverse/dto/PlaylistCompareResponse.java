package com.nos.movieuniverse.dto;

import java.math.BigDecimal;
import java.util.List;

/** Result of comparing two active playlists by mean combined rating per film slot. */
public record PlaylistCompareResponse(
        CompareSide left,
        CompareSide right,
        /** {@code left}, {@code right}, {@code tie}, or {@code insufficient} when neither side has scorable films. */
        String winner,
        int commonFilmCount,
        List<MovieResponse> commonFilms) {

    public record CompareSide(
            long playlistId,
            String name,
            String owner,
            int filmCount,
            BigDecimal averageCombinedRating,
            int scorableFilmCount) {}
}
