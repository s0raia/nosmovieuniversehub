package com.nos.movieuniverse.dto;

import jakarta.validation.constraints.NotNull;

public record AddPlaylistItemRequest(@NotNull Long tmdbId) {}
