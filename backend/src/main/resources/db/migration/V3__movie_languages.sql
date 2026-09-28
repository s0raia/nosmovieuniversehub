ALTER TABLE movie
    ADD COLUMN original_language VARCHAR(10);

ALTER TABLE movie
    ADD COLUMN spoken_languages JSONB;
