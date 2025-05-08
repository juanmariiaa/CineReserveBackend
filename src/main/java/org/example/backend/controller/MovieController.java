package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieDTO;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Movie;
import org.example.backend.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Movie", description = "Movie management APIs")
public class MovieController {
    private static final Logger log = LoggerFactory.getLogger(MovieController.class);

    private final MovieService movieService;

    @PostMapping("/tmdb/{tmdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create movie from TMDB", description = "Creates a new movie by fetching data from TMDB API (Admin only)")
    public ResponseEntity<Movie> createMovieFromTMDB(@PathVariable Integer tmdbId) {
        Movie movie = movieService.createMovieFromTMDB(tmdbId);
        return ResponseEntity.ok(movie);
    }

    @GetMapping
    @Operation(summary = "Get all movies", description = "Retrieves a list of all active movies")
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        log.info("Fetching all movies from database");
        try {
            List<Movie> movies = movieService.getAllActiveMovies();
            List<MovieDTO> movieDTOs = movies.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(movieDTOs);
        } catch (Exception e) {
            log.error("Error fetching movies: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID", description = "Retrieves a single movie by its ID")
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies", description = "Searches for movies by name in TMDB database")
    public ResponseEntity<List<MovieSearchResponse.MovieResult>> searchMovies(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            List<MovieSearchResponse.MovieResult> results = movieService.findMoviesByName(name);
            return ResponseEntity.ok(results != null ? results : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList()); // Return empty list instead of error
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a movie", description = "Deletes a movie by its ID (Admin only)")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        log.info("Deleting movie with id: {}", id);
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting movie: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setBackdropUrl(movie.getBackdropUrl());
        dto.setRating(movie.getRating());
        dto.setLanguage(movie.getLanguage());
        dto.setDirector(movie.getDirector());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setTmdbId(movie.getTmdbId());
        dto.setImdbId(movie.getImdbId());
        dto.setPopularity(movie.getPopularity());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setVoteCount(movie.getVoteCount());

        // Solo extraemos los nombres de los géneros
        if (movie.getGenres() != null) {
            movie.getGenres().forEach(genre -> dto.getGenreNames().add(genre.getName()));
        }

        return dto;
    }
}
