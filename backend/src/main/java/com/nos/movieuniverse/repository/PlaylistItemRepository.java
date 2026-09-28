package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.model.PlaylistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    /** How often each film appears across all playlists (including duplicates). */
    @Query("""
            SELECT pi.movie.tmdbId AS tmdbId, COUNT(pi) AS appearances
            FROM PlaylistItem pi
            GROUP BY pi.movie.tmdbId
            ORDER BY COUNT(pi) DESC
            """)
    List<MovieAppearance> countAppearancesByMovie();

    interface MovieAppearance {
        Long getTmdbId();

        long getAppearances();
    }

    List<PlaylistItem> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    /**
     * Looks a row up by its natural key. Not by film: the same film may occupy
     * two positions in one playlist.
     */
    Optional<PlaylistItem> findByPlaylistIdAndPosition(Long playlistId, int position);

    List<PlaylistItem> findByPlaylistIdAndMovieTmdbId(Long playlistId, Long tmdbId);

    int countByPlaylistId(Long playlistId);
}
