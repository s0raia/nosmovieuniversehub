package com.nos.movieuniverse.dto;

/** One row in a playlist, with stable {@code itemId} for remove/move. */
public record PlaylistItemResponse(long itemId, int position, MovieResponse film) {}
