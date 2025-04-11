package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Movie;
import org.example.backend.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;

    @PostMapping("/tmdb/{tmdbId}")
    public ResponseEntity<Movie> createMovieFromTMDB(@PathVariable Integer tmdbId) {
        Movie movie = movieService.createMovieFromTMDB(tmdbId);
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResponse.MovieResult>> searchMovies(@RequestParam String name) {
        List<MovieSearchResponse.MovieResult> results = movieService.findMoviesByName(name);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Movie> deleteMovie(@PathVariable Long id) {
        Movie movie = movieService.deleteMovieById(id);
        return ResponseEntity.ok(movie);
    }
}