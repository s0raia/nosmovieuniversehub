package com.nos.movieuniverse.seed;

/**
 * What one import actually changed.
 *
 * <p>A re-import of an unchanged file must report zero everywhere. That is the
 * observable definition of idempotency used by the tests and printed to the log
 * so the behaviour is visible without a debugger.
 */
public record SeedImportSummary(
        int usersCreated,
        int movieStubsCreated,
        int playlistsCreated,
        int playlistsUpdated,
        int itemsCreated,
        int itemsUpdated,
        int ratingsCreated,
        int ratingsUpdated) {

    public boolean changedNothing() {
        return usersCreated == 0
                && movieStubsCreated == 0
                && playlistsCreated == 0
                && playlistsUpdated == 0
                && itemsCreated == 0
                && itemsUpdated == 0
                && ratingsCreated == 0
                && ratingsUpdated == 0;
    }

    @Override
    public String toString() {
        if (changedNothing()) {
            return "nothing to do, everything already present";
        }
        return "users +%d, film stubs +%d, playlists +%d/~%d, items +%d/~%d, ratings +%d/~%d"
                .formatted(
                        usersCreated,
                        movieStubsCreated,
                        playlistsCreated,
                        playlistsUpdated,
                        itemsCreated,
                        itemsUpdated,
                        ratingsCreated,
                        ratingsUpdated);
    }
}
