# MovieUniverse Hub

A web app that searches films through the TMDB API, lets users build personal
playlists and rate films, and computes a **combined rating** that weighs TMDB's
average against ratings from this app's own users, taking vote counts into
account on both sides. Playlists can be compared against each other.

Built as an individual exercise. Backend in Spring Boot, frontend in React,
data in PostgreSQL.

> Status: work in progress. The schema, Docker setup and frontend shell exist.
> The TMDB client, seed importer and combined-rating calculation are not
> implemented yet.

## Requirements

- Docker Desktop
- Java 25 and Maven (only for running the backend natively)
- Node 22+ (only for running the frontend natively)
- A free TMDB account, for an **API Read Access Token**

## Setup

```bash
cp .env.example .env
```

Fill in `TMDB_API_READ_TOKEN` with your TMDB **API Read Access Token** (the
bearer token, not the v3 API key). The database credentials can be left empty;
they default to `movieuniverse`. `.env` is gitignored and must never be
committed.

If something already listens on port 5432 on your machine, such as a locally
installed PostgreSQL, uncomment `POSTGRES_PORT` in `.env` and set another port.

## Running

Two modes, from a single `docker-compose.yml`.

**Development** starts PostgreSQL only, so the backend and frontend run
natively with hot reload:

```bash
docker compose up -d                  # PostgreSQL
cd backend  && ./mvnw spring-boot:run # http://localhost:8080
cd frontend && npm install && npm run dev  # http://localhost:5173
```

**Full stack** runs all three in containers:

```bash
docker compose --profile full up
```

The frontend is then on <http://localhost:3000> and the backend on
<http://localhost:8080>.

## Project layout

```
backend/     Spring Boot 4.1.1, Java 25, Maven, Flyway
frontend/    React + Vite + TypeScript, CSS Modules
data/        seed_playlists.json, provided with the exercise and unmodified
```

## Database

Flyway owns the schema and the application owns the data. Migrations live in
`backend/src/main/resources/db/migration/` and run automatically at startup.
Hibernate is set to `ddl-auto: validate`, so it only checks that entities match
the tables rather than creating them.

The schema is shaped by the provided seed file, which contains several
deliberate awkward cases:

- A film appears **twice in the same playlist** (`pl-01` holds `27205` at
  positions 1 and 6), so `playlist_item` uses a surrogate key and is unique on
  `(playlist_id, position)`, never on `(playlist_id, tmdb_id)`.
- Some playlists are marked deleted, and four films exist only inside them, so
  playlists use soft delete via `deleted_at`.
- Two films are called Dune and two are called The Lion King, so `tmdb_id` is
  the only identifier and release date is stored to tell them apart.
- The import must be repeatable, so every row has a natural upsert key.

TMDB returns `vote_average: 0.0` for a film nobody has voted on. Storing that
as-is would show "0" where the requirement is "no votes", so the absence of
votes is stored as `NULL` and the database enforces it:
`CHECK ((tmdb_vote_count = 0) = (tmdb_vote_average IS NULL))`.

## Interface

The layout borrows from Trakt: dense poster grids with the rating visible on
the card itself. The treatment is neo-brutalist, with flat fills, 4px borders,
hard 6px offset shadows and no gradients.

Styling uses **CSS Modules** exclusively. Design tokens live in
`frontend/src/styles/tokens.css`.

The palette follows a navy-ink plus one-main plus one-accent structure, using
teal as the main and magenta as the accent. Gold was deliberately left out so
that poster artwork remains the only source of visual heat in the grid.

Every colour used behind text was measured against both surface colours and
clears **7:1 (WCAG AAA)**:

- Ink `#0B1F24` — 15.77 on paper
- Teal `#0F4C5C` — 8.82 on paper
- Teal fill `#12594E` — 8.19 with white text
- Magenta `#7A1E5C` — 8.99 on paper
- Bordeaux `#6B1D36` — 10.51 on paper
- Muted text `#3D4F55` — 7.96 on paper

Measured ratios are recorded in `tokens.css`; recheck them if a value changes.
Every interactive class defines its own `:focus-visible` state, a 3px magenta
ring offset from the border.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.
