package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.domain.PlaylistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    List<PlaylistItem> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    /**
     * Looks a row up by its natural key. Not by film: the same film may occupy
     * two positions in one playlist.
     */
    Optional<PlaylistItem> findByPlaylistIdAndPosition(Long playlistId, int position);
}
