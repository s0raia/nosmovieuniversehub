# DECISIONS.md

English notes for reviewers. Seed playlist **names** stay Portuguese because they are stored data, not UI strings.

## Stack (brief)

Spring Boot 4, Java 25, PostgreSQL, Flyway, session auth + CSRF, React + Vite, CSS Modules. Movies only (TMDB movie API).

## Combined rating

Two-step blend: pool TMDB and local votes by count, then shrink toward catalogue mean with prior weight **m = 1000** and **C = 6.5**. Implemented in `CombinedRatingCalculator` (no Spring, no HTTP). When total votes are zero, there is no combined score. Unit tests cover 8.9/12 vs 8.4/30 000 ordering.

## Vote count question

A TMDB average without its vote count is misleading. The UI always shows average **and** count, or “no votes”. The importer stores `NULL` average when `vote_count = 0` because TMDB sends `0.0`, not null.

## Seed traps

- **Duplicate film in one playlist (`pl-01`):** surrogate `playlist_item.id`, unique `(playlist_id, position)` deferrable.
- **Soft-deleted playlists:** `deleted_at`; no global `@SQLRestriction` so the importer can read/write deleted rows.
- **Same title, different years:** primary key is `tmdb_id`; UI shows `Title (Year)`.

## Star icon and playlist items

The star on film cards opens a **playlist picker**: add or remove the film in any of the member’s active playlists via `POST/DELETE /api/me/playlists/{id}/items`. Each row in playlist payloads includes an `itemId` so remove and move work when the same `tmdb_id` appears twice in one list (seed trap).

**Starred picks** (`external_id` `starred-{username}`) remains a dedicated list; `POST/DELETE /api/me/starred/{tmdbId}` is a shortcut to add/remove there. **Move** between lists: `POST /api/me/playlist-items/{itemId}/move` with `{ targetPlaylistId }`.

## Deletes

No user API to delete whole playlists, catalogue films, or rating rows. `DELETE /api/me/starred/{tmdbId}` only removes membership from Starred picks.

## Playlist comparison

The briefing requires comparing two lists but does **not** require login for that feature. Compare is **read-only**: `GET /api/playlists` and `GET /api/playlists/compare` stay public; only **active** playlists (non-deleted) from registered app users (seed, demo, and anyone who signed up) appear in the picker. Creating playlists, starring, and rating still require a session, as briefing Section 6 asks when password login is used.

Winner = higher mean **combined** rating across films in the playlist (slots without a combined score skipped). Also shows films in common.

## Game (optional briefing item)

Deferred: `game_score` table exists; no game UI in this delivery.

## Three deliberate product choices

1. **English UI**, Portuguese seed keys quarantined in Jackson DTOs.
2. **TMDB metadata in PostgreSQL** as cache, not live-only API.
3. **Compose profiles:** Postgres by default; `full` profile for containerised backend/frontend.
