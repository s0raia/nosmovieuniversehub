package com.nos.movieuniverse.controller;

import com.nos.movieuniverse.dto.HomeSectionResponse;
import com.nos.movieuniverse.service.HomeService;
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
