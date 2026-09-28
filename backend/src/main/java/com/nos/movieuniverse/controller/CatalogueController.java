package com.nos.movieuniverse.controller;

import com.nos.movieuniverse.dto.MovieDetailResponse;
import com.nos.movieuniverse.dto.MovieResponse;
import com.nos.movieuniverse.dto.PlaylistCompareResponse;
import com.nos.movieuniverse.dto.PlaylistResponse;
import com.nos.movieuniverse.service.CatalogueService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only catalogue endpoints. Writing, and the auth that has to guard it, come later. */
@RestController
@RequestMapping("/api")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/movies")
    public List<MovieResponse> movies() {
        return catalogueService.findAllMovies();
    }

    @GetMapping("/movies/{tmdbId}")
    public MovieDetailResponse movie(@PathVariable long tmdbId) {
        return catalogueService.findMovieDetail(tmdbId);
    }

    @GetMapping("/playlists")
    public List<PlaylistResponse> playlists() {
        return catalogueService.findActivePlaylists();
    }

    @GetMapping("/playlists/compare")
    public PlaylistCompareResponse comparePlaylists(
            @RequestParam("left") long leftId, @RequestParam("right") long rightId) {
        return catalogueService.comparePlaylists(leftId, rightId);
    }
}
