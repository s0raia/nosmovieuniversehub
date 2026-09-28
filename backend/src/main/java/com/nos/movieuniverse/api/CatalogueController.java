package com.nos.movieuniverse.api;

import com.nos.movieuniverse.api.dto.MovieDetailResponse;
import com.nos.movieuniverse.api.dto.MovieResponse;
import com.nos.movieuniverse.api.dto.PlaylistResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
