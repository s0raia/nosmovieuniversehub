package com.nos.movieuniverse.rating;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Blends TMDB's rating with local users' ratings into a single score.
 *
 * <p>A plain average of the two would let a film rated 10 by one local user
 * outrank a classic with thirty thousand votes, so vote counts have to carry
 * weight. The calculation is in two steps.
 *
 * <p>First the votes are pooled, weighting each average by how many votes stand
 * behind it:
 *
 * <pre>
 *   V     = tmdbVotes + localVotes
 *   Rpool = (tmdbVotes x tmdbAverage + localVotes x localAverage) / V
 * </pre>
 *
 * <p>Then the pooled average is pulled toward the catalogue mean by an amount
 * that shrinks as the vote count grows - the standard Bayesian correction:
 *
 * <pre>
 *   combined = (V x Rpool + m x C) / (V + m)
 * </pre>
 *
 * <p>With {@code m = 1000} a film needs real support before its average is
 * taken at face value. 8.9 from 12 votes lands at 6.53, while 8.4 from 30,000
 * lands at 8.34, so the widely-rated film ranks higher as it should. Three
 * local 10s move the first only to 6.54: nowhere near enough to flip the order.
 *
 * <p>Deliberately free of Spring, repositories and HTTP. The guessing game has
 * to produce exactly the same number as the listings, and the only way to
 * guarantee that is for both to call this.
 */
public final class CombinedRatingCalculator {

    /**
     * Prior weight, in votes. A film with this many votes is pulled halfway
     * toward the catalogue mean.
     */
    public static final BigDecimal PRIOR_WEIGHT = BigDecimal.valueOf(1000);

    /** The mean a film is assumed to sit at before any evidence arrives. */
    public static final BigDecimal CATALOGUE_MEAN = BigDecimal.valueOf(6.5);

    /** Matches the precision of the stored TMDB average. */
    private static final int SCALE = 3;

    private CombinedRatingCalculator() {
    }

    /**
     * @return the combined score, or empty when nothing has been rated at all.
     *     Empty means "not enough information", which the interface must show as
     *     such rather than as a zero.
     */
    public static Optional<BigDecimal> combine(RatingInput input) {
        BigDecimal totalVotes = BigDecimal.valueOf(input.totalVotes());
        if (totalVotes.signum() == 0) {
            return Optional.empty();
        }

        BigDecimal weightedSum = input
                .tmdbContribution()
                .add(input.localContribution());

        BigDecimal combined = weightedSum
                .add(PRIOR_WEIGHT.multiply(CATALOGUE_MEAN))
                .divide(totalVotes.add(PRIOR_WEIGHT), SCALE, RoundingMode.HALF_UP);

        return Optional.of(combined);
    }
}
