package com.nos.movieuniverse.controller;

import com.nos.movieuniverse.dto.AddPlaylistItemRequest;
import com.nos.movieuniverse.dto.CreatePlaylistRequest;
import com.nos.movieuniverse.dto.MovePlaylistItemRequest;
import com.nos.movieuniverse.dto.PlaylistItemResponse;
import com.nos.movieuniverse.dto.PlaylistResponse;
import com.nos.movieuniverse.dto.StarredIdsResponse;
import com.nos.movieuniverse.dto.UpdatePlaylistRequest;
import com.nos.movieuniverse.dto.UpsertRatingRequest;
import com.nos.movieuniverse.dto.UserRatingResponse;
import com.nos.movieuniverse.service.CatalogueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated user mutations. There is no user-facing delete for whole
 * playlists, catalogue films, or rating rows. Playlist membership is managed via
 * item endpoints; {@code /starred} is a shortcut for the {@code Starred picks} list.
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CatalogueService catalogueService;

    public MeController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/playlists")
    public List<PlaylistResponse> myPlaylists(@AuthenticationPrincipal UserDetails user) {
        return catalogueService.findPlaylistsForUser(user.getUsername());
    }

    @PostMapping("/playlists")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse createPlaylist(
            @AuthenticationPrincipal UserDetails user, @Valid @RequestBody CreatePlaylistRequest request) {
        return catalogueService.createPlaylist(user.getUsername(), request.name());
    }

    @PatchMapping("/playlists/{id}")
    public PlaylistResponse renamePlaylist(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable long id,
            @Valid @RequestBody UpdatePlaylistRequest request) {
        return catalogueService.renamePlaylist(user.getUsername(), id, request.name());
    }

    @PostMapping("/playlists/{playlistId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistItemResponse addPlaylistItem(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable long playlistId,
            @Valid @RequestBody AddPlaylistItemRequest request) {
        return catalogueService.addPlaylistItem(user.getUsername(), playlistId, request.tmdbId());
    }

    @DeleteMapping("/playlists/{playlistId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlaylistItem(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable long playlistId,
            @PathVariable long itemId) {
        catalogueService.removePlaylistItem(user.getUsername(), playlistId, itemId);
    }

    @PostMapping("/playlist-items/{itemId}/move")
    public PlaylistItemResponse movePlaylistItem(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable long itemId,
            @Valid @RequestBody MovePlaylistItemRequest request) {
        return catalogueService.movePlaylistItem(user.getUsername(), itemId, request.targetPlaylistId());
    }

    @GetMapping("/starred")
    public StarredIdsResponse starred(@AuthenticationPrincipal UserDetails user) {
        return catalogueService.findStarredTmdbIds(user.getUsername());
    }

    @PostMapping("/starred/{tmdbId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void starFilm(@AuthenticationPrincipal UserDetails user, @PathVariable long tmdbId) {
        catalogueService.addStarredFilm(user.getUsername(), tmdbId);
    }

    @DeleteMapping("/starred/{tmdbId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unstarFilm(@AuthenticationPrincipal UserDetails user, @PathVariable long tmdbId) {
        catalogueService.removeStarredFilm(user.getUsername(), tmdbId);
    }

    @GetMapping("/ratings/{tmdbId}")
    public UserRatingResponse myRating(@AuthenticationPrincipal UserDetails user, @PathVariable long tmdbId) {
        return catalogueService.findMyRating(user.getUsername(), tmdbId);
    }

    @PutMapping("/ratings/{tmdbId}")
    public UserRatingResponse rateFilm(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable long tmdbId,
            @Valid @RequestBody UpsertRatingRequest request) {
        return catalogueService.upsertMyRating(user.getUsername(), tmdbId, request.stars());
    }
}
