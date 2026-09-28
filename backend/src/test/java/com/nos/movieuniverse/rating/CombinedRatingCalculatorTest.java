package com.nos.movieuniverse.rating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CombinedRatingCalculatorTest {

    private static BigDecimal combined(RatingInput input) {
        return CombinedRatingCalculator.combine(input).orElseThrow();
    }

    @Test
    @DisplayName("the briefing's example: few votes must not beat many")
    void popularFilmOutranksWellRatedObscurity() {
        BigDecimal obscure = combined(RatingInput.tmdbOnly(12, new BigDecimal("8.9")));
        BigDecimal popular = combined(RatingInput.tmdbOnly(30_000, new BigDecimal("8.4")));

        assertThat(obscure).isEqualByComparingTo("6.528");
        assertThat(popular).isEqualByComparingTo("8.339");

        // The whole point of the correction: 8.9 loses to 8.4.
        assertThat(popular).isGreaterThan(obscure);
    }

    @Test
    @DisplayName("a handful of local tens cannot rescue a thinly rated film")
    void localVotesCarryWeightButNotEnoughToFlipTheOrder() {
        RatingInput withLocalPraise = new RatingInput(12, new BigDecimal("8.9"), 3, BigDecimal.TEN);

        BigDecimal before = combined(RatingInput.tmdbOnly(12, new BigDecimal("8.9")));
        BigDecimal after = combined(withLocalPraise);

        assertThat(after).isGreaterThan(before);
        assertThat(after).isEqualByComparingTo("6.539");
        assertThat(after).isLessThan(combined(RatingInput.tmdbOnly(30_000, new BigDecimal("8.4"))));
    }

    @Test
    @DisplayName("nothing rated means no score, not a zero")
    void noVotesYieldsNoScore() {
        assertThat(CombinedRatingCalculator.combine(RatingInput.none())).isEmpty();
    }

    @Test
    @DisplayName("local ratings alone still produce a score")
    void localOnlyIsEnough() {
        BigDecimal score = combined(new RatingInput(0, null, 5, new BigDecimal("9.0")));

        // Five votes against a prior of a thousand barely move the mean.
        assertThat(score).isEqualByComparingTo("6.512");
    }

    @Test
    @DisplayName("votes are pooled, so each side counts in proportion to its size")
    void averagesAreWeightedByVoteCount() {
        // Equal averages on both sides must leave the pooled average unchanged.
        BigDecimal balanced = combined(new RatingInput(100, new BigDecimal("8.0"), 100, new BigDecimal("8.0")));
        BigDecimal single = combined(RatingInput.tmdbOnly(200, new BigDecimal("8.0")));

        assertThat(balanced).isEqualByComparingTo(single);
    }

    @Test
    @DisplayName("a vast number of votes converges on the raw average")
    void largeVoteCountsOverwhelmThePrior() {
        BigDecimal score = combined(RatingInput.tmdbOnly(10_000_000, new BigDecimal("7.5")));

        assertThat(score).isCloseTo(new BigDecimal("7.5"), org.assertj.core.data.Offset.offset(new BigDecimal("0.001")));
    }

    @Test
    @DisplayName("an average without votes behind it is rejected outright")
    void inconsistentInputIsRefused() {
        assertThatThrownBy(() -> new RatingInput(5, null, 0, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RatingInput(-1, null, 0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the result is returned, never null")
    void returnsOptionalNotNull() {
        Optional<BigDecimal> result = CombinedRatingCalculator.combine(RatingInput.tmdbOnly(1, BigDecimal.ONE));
        assertThat(result).isPresent();
    }
}
