package com.nos.movieuniverse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** One user's rating of one film. At most one row per user and film. */
@Entity
public class UserRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tmdb_id", nullable = false)
    private Movie movie;

    /** 1 to 10 inclusive, matching the seed's "estrelas". */
    @Column(nullable = false)
    private short stars;

    /** The seed's "data": a calendar day, with no time or zone. */
    @Column(nullable = false)
    private LocalDate ratedAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected UserRating() {
        // required by JPA
    }

    public UserRating(AppUser user, Movie movie, short stars, LocalDate ratedAt) {
        this.user = user;
        this.movie = movie;
        this.stars = stars;
        this.ratedAt = ratedAt;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Movie getMovie() {
        return movie;
    }

    public short getStars() {
        return stars;
    }

    public void setStars(short stars) {
        this.stars = stars;
    }

    public LocalDate getRatedAt() {
        return ratedAt;
    }

    public void setRatedAt(LocalDate ratedAt) {
        this.ratedAt = ratedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
