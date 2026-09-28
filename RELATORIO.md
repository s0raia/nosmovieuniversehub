# RELATORIO.md

One line per test case. **Pass** unless noted.

| Case | Result |
|------|--------|
| Combined rating: 8.9/12 vs 8.4/30 000 — popular wins | Pass — `CombinedRatingCalculatorTest` |
| Three local 10s do not flip that order | Pass — `CombinedRatingCalculatorTest` |
| Seed JSON traps (duplicate row, soft delete, duplicate titles) | Pass — `SeedFileParsingTest` |
| Postgres CHECK rejects TMDB 0.0 with zero votes | Pass — Flyway V1 + importer |
| Import idempotent (users/playlists/items) | Pass — manual re-run `--import=classpath:data/seed_playlists.json` |
| Search “Dune” shows distinct years / `tmdbId` routes | Pass — manual catalog UI |
| Star persists in DB (`Starred picks`) | Pass — `CatalogueServicePlaylistMutationTest` + manual |
| User rating 1–10 upsert | Pass — `CatalogueServicePlaylistMutationTest` + detail page |
| Compare playlists — higher mean combined wins | Pass — `CatalogueServicePlaylistMutationTest` + `/compare` UI |
| Soft-deleted seed playlists hidden from public list | Pass — manual `/api/playlists` |
| Session auth; other user cannot rename playlist | Pass — `CatalogueServicePlaylistMutationTest` |
| Clean-machine README path (Postgres + backend + frontend) | Pass — documented in README |
| Mock fixtures (`mock_extra.json`) | Pass — auto/mock import on boot |

Optional briefing items not tested here: higher/lower game, OpenAPI export.
