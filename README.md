# MovieUniverse Hub

A web app built for a practical exercise in the **Specialisterne** program: browse films via the [TMDB API](https://www.themoviedb.org/), manage personal playlists, rate titles, and view a **combined rating** (TMDB average plus this app’s user ratings). Backend: Spring Boot. Frontend: React. Database: PostgreSQL.

## Context

This repository is my submission for that exercise. [`data/seed_playlists.json`](data/seed_playlists.json) is the provided seed file and is **not modified**; extra demo users and playlists are in [`data/mock_extra.json`](data/mock_extra.json). The exercise allows AI assistance; I used **Cursor (Composer 2.5)** to help plan and implement parts of the work. The UI and docs use a mix of British and American English in places.

## Quick start

1. `cp .env.example .env` and set `TMDB_API_READ_TOKEN` (TMDB **Read Access Token**).
2. `docker compose up -d`
3. `cd backend && ./mvnw spring-boot:run`
4. `cd frontend && npm install && npm run dev`
5. Open http://localhost:5173

If port 5432 is busy, set `POSTGRES_PORT` in `.env`. Seeded users (`ana`, `bruno`, `carla`) use `SEED_DEFAULT_PASSWORD`, or **`movieuniverse`** when that variable is empty.

## Full stack (optional)

```bash
docker compose --profile full up
```

Frontend: http://localhost:3000 · Backend: http://localhost:8080

## Features

- Idempotent seed import, Flyway schema, TMDB enrichment of film stubs
- Home sections, catalog with filters, film detail (genres, runtime, spoken languages when enriched)
- Session register / login / logout; create and rename your playlists (any language for names you create)
- Combined rating calculator; seed playlist titles remain Portuguese from the provided seed file
- Mock and persona demo accounts loaded from `mock_extra.json` on first boot (or when missing users are detected)

## Demo logins

Use **`movieuniverse`** when `SEED_DEFAULT_PASSWORD` is unset (unless you configured your own). Seed users: `ana`, `bruno`, `carla`. Mock users: `mock-01` … `mock-17` (display names such as Jimmy, Maite, Yuki). Persona demos: `joao`, `sabrina`, `andres`, `soraia` (playlists themed for demos; `soraia` reflects my Trakt movie export). To refresh mock data on an existing DB:

```bash
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--import=classpath:data/mock_extra.json
```

## Project layout

```
backend/   config, controller, dto, model, repository, service, seed, tmdb
frontend/  api, layouts, pages, types, components, contexts (CSS Modules)
data/      seed_playlists.json (provided), mock_extra.json (demo extension)
```

## Seed data (short)

The schema matches deliberate seed traps: duplicate titles, a duplicate row in one playlist, soft-deleted playlists, and TMDB averages stored as `NULL` when there are no votes.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB. Film metadata and posters © TMDB contributors.

---

> **Thanks for looking!**
>
> *- s0raia*

---
