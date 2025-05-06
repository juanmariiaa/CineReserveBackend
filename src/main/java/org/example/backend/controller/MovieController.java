package org.example.backend.controller;

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
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
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