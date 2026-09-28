package com.nos.movieuniverse.dto;

import jakarta.validation.constraints.NotNull;

public record MovePlaylistItemRequest(@NotNull Long targetPlaylistId) {}
