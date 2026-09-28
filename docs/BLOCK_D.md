# Block D — Auth, mock users, themed playlists

Plan for after the catalogue demo (coach / 14:00). Estimates assume one focused
day, not the 7-minute presentation window.

## Goals

1. **Login and register** in the UI (session cookies, Spring Security form login).
2. **~20 users** in the database: keep seed `ana`, `bruno`, `carla` plus **17**
   mock users (`mock-01` … `mock-17`), all with a documented dev password.
3. **Themed playlists** beyond the seed — e.g. "90's Nostalgia", "Best action
   movies of the 80s", "Favorite cartoon/Disney movies", "To watch later",
   "Never watching these again", plus a few empty or overlapping lists for
   comparison demos.
4. **Idempotent import** via `data/mock_extra.json` and
   `--import=classpath:data/mock_extra.json` (same DTOs as the seed).

Out of scope for Block D unless time remains: comments, favorites table, game
UI, Docker full-profile polish.

---

## Backend (order matters)

### D1 — Session auth (~3–4 h)

- `SecurityFilterChain`: permit `GET /api/movies`, `/api/playlists` (or require
  auth later), expose `POST /api/auth/register`, form login
  `POST /login` (Spring default or custom JSON).
- `UserDetailsService` loading `app_user` by username; BCrypt passwords.
- Register: validate username unique, password length, store hash; no TMDB key
  in logs.
- Re-enable **CSRF** for browser POSTs; expose token to React (cookie or
  `/api/auth/csrf`).
- Integration test: register → login → session cookie → optional protected
  endpoint.

### D2 — `mock_extra.json` (~2–3 h)

- Same JSON shape as `seed_playlists.json` (`utilizadores`, `playlists`,
  `notas`); **do not** edit the committed seed file.
- **17 users** with distinct usernames; reuse **tmdb_id** values from the seed
  catalogue (and 1–2 zero-vote films once identified via TMDB discover).
- **Playlists** (examples — assign across users):

  | Theme | Owner idea | Notes |
  |-------|------------|--------|
  | 90's Nostalgia | mock-01 | 8–12 films from seed IDs |
  | Best action of the 80s | mock-02 | overlap with seed OK |
  | Favorite cartoon / Disney | mock-03 | include 8587 / 420818 |
  | To watch later | mock-04 | empty or 2–3 stubs |
  | Never watching these again | mock-05 | humorous picks |
  | Sci-fi marathon | bruno or mock-06 | cross-user |
  | Date night | mock-07 | |
  | … | mock-08–17 | vary sizes, one deleted playlist |

- Copy `mock_extra.json` to classpath via existing Maven resources plugin
  (`data/*.json`).
- Document password in README (`SEED_DEFAULT_PASSWORD` or shared mock password).

### D3 — Playlist API for logged-in user (~2 h)

- `GET /api/me/playlists` (active only unless `?includeDeleted=` for admin demo).
- Optional: `POST /api/playlists` create — only if register flow needs a empty
  list for new users.

---

## Frontend (~3–4 h)

### D4 — Auth pages (CSS Modules)

- `/login` and `/register` routes (React Router or lightweight manual routing).
- Forms: username, password; register confirm password.
- On success, redirect to catalogue; show username in header; **Logout** POST.
- Replace nav "Playlists (soon)" with real `/playlists` when D5 exists.

### D5 — Playlists view

- List current user's playlists (name, film count); drill-down to film grid
  reusing `MovieCard`.
- Read-only for Block D is enough; add/remove film is Block E.

---

## Git / deliverables

- Branch: `feature/auth-and-mock-data` → PR to `main`.
- Update README quick start with mock import line:
  `java -jar … --import=classpath:data/mock_extra.json` or document
  ApplicationRunner flag.
- `DECISIONS.md`: session vs JWT, CSRF choice, mock password policy.
- No TMDB token in repo or AI transcripts.

---

## Demo script additions (after Block D)

- Log in as `mock-01`, open "90's Nostalgia".
- Show register creating a new user with empty catalogue.
- Mention 20 accounts and themed lists loaded from JSON, same importer as seed.

---

## Risks

| Risk | Mitigation |
|------|------------|
| 20 users + playlists slow to author by hand | Generate JSON from a small script listing tmdb_ids only |
| CSRF breaks Vite proxy | Use Spring Security CSRF cookie repository + fetch header |
| Register spam in prod | Out of scope; exercise is local/demo |

---

## Suggested time box

| Block | Hours |
|-------|-------|
| D1 Auth | 4 |
| D2 mock JSON + import | 3 |
| D3 API | 2 |
| D4–D5 UI | 4 |
| **Total** | **~2 days** part-time |

For a **same-day** stretch: ship D1 + login UI + **5 mock users** and **3
themes** first; expand to 17 users in a follow-up commit.
