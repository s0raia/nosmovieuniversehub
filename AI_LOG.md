# AI_LOG.md

At least three cases where AI suggestions were wrong or rejected. All verified against this repository.

## 1. CHECK constraint that accepted 0.0

**Suggested:** `CHECK (tmdb_vote_count = 0 OR tmdb_vote_average IS NOT NULL)`.

**Problem:** When count is zero the first clause is true, so the constraint passes and allows `0.0` — the bug the briefing warns about.

**Instead:** Equivalence `CHECK ((tmdb_vote_count = 0) = (tmdb_vote_average IS NULL))`, verified in Postgres.

## 2. Composite primary key on playlist items

**Suggested:** `PRIMARY KEY (playlist_id, tmdb_id)` matching the briefing diagram.

**Problem:** Seed `pl-01` lists the same `tmdb_id` twice; that schema cannot import the provided file.

**Instead:** Surrogate key on `playlist_item` plus unique `(playlist_id, position)`.

## 3. `@SQLRestriction` for soft-deleted playlists

**Suggested:** Hibernate filter so deleted playlists never appear.

**Problem:** The importer must insert and re-read `pl-03` / `pl-07` as deleted for idempotent import.

**Instead:** Explicit `deleted_at IS NULL` in repository query methods.

## 4. Wrong Spring Dotenv artifact (silent failure)

**Suggested:** `me.paulschwarz:spring-dotenv:5.1.0` only.

**Problem:** Boot 4 needs `springboot4-dotenv`; the core JAR never registers an initializer, so `${POSTGRES_USER}` silently used defaults.

**Instead:** `springboot4-dotenv` plus `springdotenv.directory: ..` in `application.yml`.
