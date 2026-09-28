package com.nos.movieuniverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePlaylistRequest(@NotBlank @Size(min = 1, max = 120) String name) {}
