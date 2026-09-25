package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.domain.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Soft deletion is filtered per query rather than with {@code @SQLRestriction}.
 * A global filter would hide pl-03 and pl-07 from the importer itself, which has
 * to write them as deleted and then read them back to stay idempotent.
 */
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /** The seed's own id, such as "pl-01". The upsert key for re-imports. */
    Optional<Playlist> findByExternalId(String externalId);

    List<Playlist> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

    Optional<Playlist> findByIdAndDeletedAtIsNull(Long id);

    List<Playlist> findByDeletedAtIsNull();
}
