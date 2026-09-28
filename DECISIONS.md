# Design decisions

## Language

- **User interface:** American English (labels, navigation, errors, README).
- **Seed playlist titles:** Stored verbatim from the provided NOS seed JSON (Portuguese names such as *Ficção científica*). These are **user data**, not missing translations.
- **User-created playlist names:** Any language and Unicode (including emoji), trimmed, 1–120 characters. No locale validation on the name field.

## Playlists

- Playlists imported from seed or mock JSON keep an `external_id` for idempotent re-import.
- Playlists created in the app have no `external_id`.
- Owners may rename any of their active playlists, including seed imports. Re-importing JSON may update seed-owned fields per importer rules; app-created names are unaffected.

## Auth (Block D)

- Session cookies with Spring Security; CSRF via `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header for browser POST/PATCH.
