package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Home Page", description = "APIs for the home page functionality")
public class HomePageController {

    private final HomePageService homePageService;

    @GetMapping("/screenings/today")
    @Operation(summary = "Get today's screenings", description = "Retrieves all screenings for today")
    public ResponseEntity<List<ScreeningDTO>> getTodaysScreenings() {
        List<ScreeningDTO> screenings = homePageService.getTodaysScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/screenings/featured")
    @Operation(summary = "Get featured screenings", description = "Retrieves highlighted screenings for the next week")
    public ResponseEntity<List<ScreeningDTO>> getFeaturedScreenings() {
        List<ScreeningDTO> screenings = homePageService.getFeaturedScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/movies/featured")
    @Operation(summary = "Get featured movies with screenings", description = "Retrieves featured movies along with their upcoming screenings")
    public ResponseEntity<List<MovieWithScreeningsDTO>> getFeaturedMoviesWithScreenings() {
        List<MovieWithScreeningsDTO> movies = homePageService.getFeaturedMoviesWithScreenings();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/movies/current")
    @Operation(summary = "Get current movies with screenings", description = "Retrieves currently active movies with their upcoming screenings")
    public ResponseEntity<List<MovieWithScreeningsDTO>> getCurrentMoviesWithScreenings() {
        List<MovieWithScreeningsDTO> movies = homePageService.getCurrentMoviesWithScreenings();
        return ResponseEntity.ok(movies);
    }
}