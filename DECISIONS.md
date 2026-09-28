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

## Star icon

Adds/removes the film in the member’s **Starred picks** playlist (`external_id` `starred-{username}`), persisted in `playlist_item`.

## Deletes

No user API to delete whole playlists, catalogue films, or rating rows. `DELETE /api/me/starred/{tmdbId}` only removes membership from Starred picks.

## Playlist comparison

Winner = higher mean **combined** rating across films in the playlist (slots without a combined score skipped). Also shows films in common.

## Game (optional briefing item)

Deferred: `game_score` table exists; no game UI in this delivery.

## Three deliberate product choices

1. **English UI**, Portuguese seed keys quarantined in Jackson DTOs.
2. **TMDB metadata in PostgreSQL** as cache, not live-only API.
3. **Compose profiles:** Postgres by default; `full` profile for containerised backend/frontend.
