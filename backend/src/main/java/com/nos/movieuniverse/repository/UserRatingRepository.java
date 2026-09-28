package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.model.UserRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {

    /**
     * Local vote count and mean per film, in one query. Loading every rating and
     * averaging in Java would work for a seed of twenty but not for a catalogue.
     */
    @Query("""
            SELECT r.movie.tmdbId AS tmdbId, COUNT(r) AS voteCount, AVG(r.stars) AS average
            FROM UserRating r
            GROUP BY r.movie.tmdbId
            """)
    List<LocalRatingAggregate> findLocalAggregates();

    /** Projection for {@link #findLocalAggregates()}. */
    interface LocalRatingAggregate {
        Long getTmdbId();

        long getVoteCount();

        Double getAverage();
    }

    /** The (user, film) pair is unique, and is the upsert key for re-imports. */
    Optional<UserRating> findByUserIdAndMovieTmdbId(Long userId, Long tmdbId);

    /** Local ratings for one film, the local half of the combined rating. */
    List<UserRating> findByMovieTmdbId(Long tmdbId);

    List<UserRating> findByUserId(Long userId);
}
