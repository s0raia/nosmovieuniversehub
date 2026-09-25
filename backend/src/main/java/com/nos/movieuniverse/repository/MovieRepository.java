package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.domain.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /** Stub rows the importer created, still waiting to be filled in from TMDB. */
    List<Movie> findByFetchedAtIsNull();

    /** Films that carry no TMDB votes, which must never display an average. */
    List<Movie> findByTmdbVoteCount(int tmdbVoteCount);
}
