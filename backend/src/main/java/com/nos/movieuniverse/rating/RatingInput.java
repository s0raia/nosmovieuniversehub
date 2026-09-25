package com.nos.movieuniverse.rating;

import java.math.BigDecimal;

/**
 * The two sides of a combined rating.
 *
 * <p>A null average is normal and means "no votes on this side", which is how
 * both TMDB-only and locally-rated-only films are represented. It is never a
 * zero: a zero average would be a real, very bad rating.
 *
 * @param tmdbVoteCount votes TMDB holds, zero if none.
 * @param tmdbAverage TMDB's average on a 1-10 scale, null when the count is zero.
 * @param localVoteCount ratings left by users of this application.
 * @param localAverage mean of those ratings, null when there are none.
 */
public record RatingInput(
        int tmdbVoteCount, BigDecimal tmdbAverage, int localVoteCount, BigDecimal localAverage) {

    public RatingInput {
        if (tmdbVoteCount < 0 || localVoteCount < 0) {
            throw new IllegalArgumentException("Vote counts cannot be negative");
        }
        if (tmdbVoteCount > 0 && tmdbAverage == null) {
            throw new IllegalArgumentException("A TMDB vote count needs an average to go with it");
        }
        if (localVoteCount > 0 && localAverage == null) {
            throw new IllegalArgumentException("A local vote count needs an average to go with it");
        }
    }

    /** Only TMDB has rated this film. */
    public static RatingInput tmdbOnly(int voteCount, BigDecimal average) {
        return new RatingInput(voteCount, average, 0, null);
    }

    /** Nothing has rated this film, so there is no score to show. */
    public static RatingInput none() {
        return new RatingInput(0, null, 0, null);
    }

    public int totalVotes() {
        return tmdbVoteCount + localVoteCount;
    }

    BigDecimal tmdbContribution() {
        return tmdbVoteCount == 0 ? BigDecimal.ZERO : tmdbAverage.multiply(BigDecimal.valueOf(tmdbVoteCount));
    }

    BigDecimal localContribution() {
        return localVoteCount == 0 ? BigDecimal.ZERO : localAverage.multiply(BigDecimal.valueOf(localVoteCount));
    }
}
