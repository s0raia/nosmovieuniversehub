package com.nos.movieuniverse.seed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A playlist as the seed describes it.
 *
 * @param externalId the file's own id, such as "pl-01". Stored on the playlist
 *     so a second import updates the same row instead of creating another.
 * @param ownerUsername matches a {@link SeedUser#username()}.
 * @param deleted the file's "apagada". Two playlists are marked deleted and
 *     hold films that appear nowhere else, so they must still be imported -
 *     just with a deletion timestamp.
 */
public record SeedPlaylist(
        @JsonProperty("id") String externalId,
        @JsonProperty("nome") String name,
        @JsonProperty("utilizador") String ownerUsername,
        @JsonProperty("apagada") boolean deleted,
        @JsonProperty("filmes") List<SeedPlaylistItem> items) {

    public SeedPlaylist {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
