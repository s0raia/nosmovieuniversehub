package com.nos.movieuniverse.dto;

import java.util.List;

/**
 * A playlist and the films in it, in order.
 *
 * <p>{@code films} is a list rather than a set on purpose: a playlist may hold
 * the same film at more than one position, which the seed data actually does.
 */
public record PlaylistResponse(
        Long id, String externalId, String name, String owner, int filmCount, List<MovieResponse> films) {
}
