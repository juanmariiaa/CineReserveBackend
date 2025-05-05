package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Movie;
import org.example.backend.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovieController {
    private static final Logger log = LoggerFactory.getLogger(MovieController.class);

    private final MovieService movieService;

    @PostMapping("/tmdb/{tmdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> createMovieFromTMDB(@PathVariable Integer tmdbId) {
        Movie movie = movieService.createMovieFromTMDB(tmdbId);
        return ResponseEntity.ok(movie);
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        log.info("Fetching all movies from database");
        try {
            List<Movie> movies = movieService.getAllActiveMovies();
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResponse.MovieResult>> searchMovies(@RequestParam String name) {
        log.info("Searching for movies with name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            log.warn("Empty search query provided");
            return ResponseEntity.ok(Collections.emptyList());
        }

        try {
            List<MovieSearchResponse.MovieResult> results = movieService.findMoviesByName(name);
            log.info("Found {} results for query: {}", results != null ? results.size() : 0, name);
            return ResponseEntity.ok(results != null ? results : Collections.emptyList());
        } catch (Exception e) {
            log.error("Error searching for movies: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList()); // Return empty list instead of error
        }
    }
}