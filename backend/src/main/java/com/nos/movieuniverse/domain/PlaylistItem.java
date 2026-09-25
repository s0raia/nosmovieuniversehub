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

/**
 * One film at one position in a playlist.
 *
 * <p>The surrogate id is deliberate. The same film may legitimately appear twice
 * in a playlist - the seed's pl-01 holds tmdb_id 27205 at positions 1 and 6 - so
 * there is no unique constraint on (playlist, movie). Uniqueness is on
 * (playlist, position) instead, and it is deferred so a reorder can rewrite
 * every position inside one transaction.
 */
@Entity
public class PlaylistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tmdb_id", nullable = false)
    private Movie movie;

    /** One-based, matching the seed's "ordem". */
    @Column(name = "position", nullable = false)
    private int position;

    @Column(nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();

    protected PlaylistItem() {
        // required by JPA
    }

    public PlaylistItem(Playlist playlist, Movie movie, int position) {
        this.playlist = playlist;
        this.movie = movie;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public Movie getMovie() {
        return movie;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }
}
