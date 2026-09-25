package com.nos.movieuniverse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.OffsetDateTime;

/** One completed round of the guessing game, kept for the leaderboard. */
@Entity
public class GameScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private OffsetDateTime playedAt = OffsetDateTime.now();

    protected GameScore() {
        // required by JPA
    }

    public GameScore(AppUser user, int score) {
        this.user = user;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public int getScore() {
        return score;
    }

    public OffsetDateTime getPlayedAt() {
        return playedAt;
    }
}
