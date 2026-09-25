package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.domain.GameScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    /** Leaderboard order: highest score first, earliest play breaking ties. */
    List<GameScore> findTop10ByOrderByScoreDescPlayedAtAsc();

    List<GameScore> findByUserIdOrderByScoreDesc(Long userId);
}
