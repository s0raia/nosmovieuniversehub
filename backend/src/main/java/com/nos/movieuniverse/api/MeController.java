package com.nos.movieuniverse.api;

import com.nos.movieuniverse.api.dto.PlaylistResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
