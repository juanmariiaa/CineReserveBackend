package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieWithScreeningsDTO;
import org.example.backend.dto.ScreeningDTO;
import org.example.backend.service.HomePageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HomePageController {

    private final HomePageService homePageService;

    @GetMapping("/screenings/today")
    public ResponseEntity<List<ScreeningDTO>> getTodaysScreenings() {
        List<ScreeningDTO> screenings = homePageService.getTodaysScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/screenings/featured")
    public ResponseEntity<List<ScreeningDTO>> getFeaturedScreenings() {
        List<ScreeningDTO> screenings = homePageService.getFeaturedScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/movies/featured")
    public ResponseEntity<List<MovieWithScreeningsDTO>> getFeaturedMoviesWithScreenings() {
        List<MovieWithScreeningsDTO> movies = homePageService.getFeaturedMoviesWithScreenings();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/movies/current")
    public ResponseEntity<List<MovieWithScreeningsDTO>> getCurrentMoviesWithScreenings() {
        List<MovieWithScreeningsDTO> movies = homePageService.getCurrentMoviesWithScreenings();
        return ResponseEntity.ok(movies);
    }
}