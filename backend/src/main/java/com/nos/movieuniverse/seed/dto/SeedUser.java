package com.nos.movieuniverse.seed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A seeded user. The file carries a name and nothing else - no password, no
 * email - so the importer has to supply a password of its own.
 */
public record SeedUser(@JsonProperty("nome") String username) {
}
