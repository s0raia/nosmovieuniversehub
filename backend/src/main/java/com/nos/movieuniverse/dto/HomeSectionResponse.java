package com.nos.movieuniverse.dto;

import java.util.List;

public record HomeSectionResponse(String id, String title, List<MovieResponse> films) {
}
