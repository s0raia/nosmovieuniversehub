# MovieUniverse Hub

A web app that searches films through the TMDB API, lets users build personal
playlists and rate films, and computes a **combined rating** that weighs TMDB's
average against ratings from this app's own users. Backend: Spring Boot.
Frontend: React. Database: PostgreSQL.

## Quick start

1. `cp .env.example .env` and set `TMDB_API_READ_TOKEN` (TMDB **Read Access Token**).
2. `docker compose up -d`
3. `cd backend && ./mvnw spring-boot:run`
4. `cd frontend && npm install && npm run dev`
5. Open http://localhost:5173

**TMDB:** This product uses the [TMDB API](https://www.themoviedb.org/) but is
not endorsed or certified by TMDB.

Leave `POSTGRES_USER` / `POSTGRES_PASSWORD` empty to use the defaults. If port
5432 is taken, uncomment `POSTGRES_PORT` in `.env`. Seeded users (`ana`,
`bruno`, `carla`) get the password from `SEED_DEFAULT_PASSWORD`, or the
documented fallback when it is blank.

## Running (full stack)

```bash
docker compose --profile full up
```

Frontend on http://localhost:3000, backend on http://localhost:8080.

## Project layout

```
backend/     Spring Boot 4.1.1, Flyway, JPA
frontend/    React + Vite, CSS Modules
data/        seed_playlists.json (provided, unmodified)
```

## What works today

- Flyway schema, seed import (idempotent), TMDB enrichment of film stubs
- Combined rating calculator (feature merged via PR)
- **Home** with curated sections (trending this week, most rewatched in playlists, top combined, blockbusters)
- **Film detail** page (`/movies/{tmdbId}`) with overview, genres, runtime, and ratings
- Catalogue grid, session **register / login / logout**, and **your playlists** when signed in
- On first boot after seed, **17 mock users** (`mock-01` … `mock-17`) and themed playlists from `data/mock_extra.json` (skipped if `mock-01` already exists)

Demo accounts: seed users `ana`, `bruno`, `carla`, or any `mock-NN` user — password from `SEED_DEFAULT_PASSWORD`, or **`movieuniverse`** when that variable is empty.

To re-import mock data on an existing database: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--import=classpath:data/mock_extra.json`

## Database notes

The schema follows traps in the provided seed: duplicate film in one playlist,
soft-deleted playlists, duplicate titles (two *Dune* films), and
`vote_average` stored as NULL when TMDB has no votes.

## Interface

Neo-brutalist UI (CSS Modules, WCAG AAA palette in `frontend/src/styles/tokens.css`).

## Attribution

Film data and posters from TMDB. This product uses the TMDB API but is not
endorsed or certified by TMDB.
