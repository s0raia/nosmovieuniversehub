package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.domain.UserRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRatingRepository extends JpaRepository<UserRating, Long> {

    /** The (user, film) pair is unique, and is the upsert key for re-imports. */
    Optional<UserRating> findByUserIdAndMovieTmdbId(Long userId, Long tmdbId);

    /** Local ratings for one film, the local half of the combined rating. */
    List<UserRating> findByMovieTmdbId(Long tmdbId);

    List<UserRating> findByUserId(Long userId);
}
