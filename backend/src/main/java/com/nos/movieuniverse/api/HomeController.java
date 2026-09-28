package com.nos.movieuniverse.api;

import com.nos.movieuniverse.api.dto.HomeSectionResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public List<HomeSectionResponse> home() {
        return homeService.buildHomepage();
    }
}
