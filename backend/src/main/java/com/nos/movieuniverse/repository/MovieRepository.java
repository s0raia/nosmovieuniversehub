package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.model.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres")
    List<Movie> findAllWithGenres();

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres WHERE m.fetchedAt IS NOT NULL")
    List<Movie> findAllEnrichedWithGenres();

    /** Stub rows the importer created, still waiting to be filled in from TMDB. */
    List<Movie> findByFetchedAtIsNull();

    /** Films that carry no TMDB votes, which must never display an average. */
    List<Movie> findByTmdbVoteCount(int tmdbVoteCount);

    List<Movie> findByFetchedAtIsNotNullAndSpokenLanguagesIsNull();
}
