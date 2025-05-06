package org.example.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieDetails;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Genre;
import org.example.backend.model.Movie;
import org.example.backend.repository.GenreRepository;
import org.example.backend.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieService {

    @Autowired
    private EntityManager entityManager;

    // And also add a logger
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final TMDBService tmdbService;
    private final GenreRepository genreRepository;

    @Transactional
    public Movie createMovieFromTMDB(Integer tmdbId) {
        if (movieRepository.existsByTmdbId(tmdbId)) {
            log.info("Movie with tmdbId {} already exists in the database", tmdbId);
            Movie existingMovie = movieRepository.findByTmdbId(tmdbId)
                    .orElseThrow(() -> new RuntimeException("Could not find existing movie"));

            return existingMovie;
        }

        MovieDetails details = tmdbService.getMovieDetails(tmdbId);
        Movie movie = new Movie();

        movie.setTmdbId(tmdbId);
        movie.setTitle(details.getTitle());
        movie.setDurationMinutes(details.getDurationMinutes());
        movie.setDescription(details.getDescription());
        movie.setReleaseDate(details.getReleaseDate());
        movie.setVoteAverage(BigDecimal.valueOf(details.getVoteAverage()));
        movie.setVoteCount(details.getVoteCount());
        String director = tmdbService.getDirectorName(tmdbId);
        movie.setDirector(director);
        movie.setPopularity(BigDecimal.valueOf(details.getPopularity()));
        movie.setLanguage(details.getOriginalLanguage());
        movie.setImdbId(details.getImdbId());
        movie.setTrailerUrl(tmdbService.getTrailerUrl(tmdbId));
        movie.setIsActive(true);

        if (details.getVoteAverage() != null) {
            if (details.getVoteAverage() >= 8.0) {
                movie.setRating("PG-13");
            } else if (details.getVoteAverage() >= 6.5) {
                movie.setRating("PG");
            } else {
                movie.setRating("G");
            }
        }

        if (details.getPosterUrl() != null) {
            movie.setPosterUrl("https://image.tmdb.org/t/p/w500" + details.getPosterUrl());
        }
        if (details.getBackdropPath() != null) {
            movie.setBackdropUrl("https://image.tmdb.org/t/p/original" + details.getBackdropPath());
        }

        LocalDateTime now = LocalDateTime.now();
        movie.setCreatedAt(now);
        movie.setUpdatedAt(now);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            movie.setAddedBy(username);
        }

        Movie savedMovie = movieRepository.save(movie);

        if (details.getGenres() != null) {
            for (MovieDetails.Genre genreDto : details.getGenres()) {
                try {
                    Genre genre = genreRepository.findByTmdbGenreId(genreDto.getId())
                            .orElseGet(() -> {
                                Genre newGenre = new Genre();
                                newGenre.setName(genreDto.getName());
                                newGenre.setTmdbGenreId(genreDto.getId());
                                return genreRepository.save(newGenre);
                            });

                    entityManager.detach(genre);
                    genre = genreRepository.findById(genre.getId()).orElse(genre);

                    savedMovie.getGenres().add(genre);

                } catch (Exception e) {
                    log.error("Error processing genre {}: {}", genreDto.getName(), e.getMessage());
                }
            }

            savedMovie = movieRepository.save(savedMovie);
        }

        return savedMovie;
    }


    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    public List<MovieSearchResponse.MovieResult> findMoviesByName(String name) {
        MovieSearchResponse response = tmdbService.searchMoviesByName(name);
        return response.getResults();
    }

    public List<Movie> getAllActiveMovies() {
        log.debug("Calling repository to get active movies");
        return movieRepository.findByIsActiveTrue();
    }

    public Movie updateMovie(Long id, Movie movieDetails) {
        Movie movie = getMovieById(id);

        movie.setTitle(movieDetails.getTitle());
        movie.setDurationMinutes(movieDetails.getDurationMinutes());
        movie.setDescription(movieDetails.getDescription());
        movie.setReleaseDate(movieDetails.getReleaseDate());
        movie.setPosterUrl(movieDetails.getPosterUrl());
        movie.setBackdropUrl(movieDetails.getBackdropUrl());
        movie.setRating(movieDetails.getRating());
        movie.setLanguage(movieDetails.getLanguage());
        movie.setDirector(movieDetails.getDirector());
        movie.setTrailerUrl(movieDetails.getTrailerUrl());
        movie.setIsActive(movieDetails.getIsActive());
        movie.setUpdatedAt(LocalDateTime.now());

        if (movieDetails.getGenres() != null) {
            movie.setGenres(movieDetails.getGenres());
        }

        return movieRepository.save(movie);
    }

    public Movie deactivateMovie(Long id) {
        Movie movie = getMovieById(id);
        movie.setIsActive(false);
        movie.setUpdatedAt(LocalDateTime.now());
        return movieRepository.save(movie);
    }

    public Movie activateMovie(Long id) {
        Movie movie = getMovieById(id);
        movie.setIsActive(true);
        movie.setUpdatedAt(LocalDateTime.now());
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }
}