package com.nos.movieuniverse.api.dto;

import java.util.List;

public record HomeSectionResponse(String id, String title, List<MovieResponse> films) {
}
