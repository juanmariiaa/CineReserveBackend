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

    // Y agregar también un logger
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final TMDBService tmdbService;
    private final GenreRepository genreRepository;

    @Transactional
    public Movie createMovieFromTMDB(Integer tmdbId) {
        // Verificar si la película ya existe en la base de datos
        if (movieRepository.existsByTmdbId(tmdbId)) {
            log.info("La película con tmdbId {} ya existe en la base de datos", tmdbId);
            Movie existingMovie = movieRepository.findByTmdbId(tmdbId)
                    .orElseThrow(() -> new RuntimeException("No se pudo encontrar la película existente"));

            return existingMovie;
        }

        // Si no existe, obtener detalles de TMDB
        MovieDetails details = tmdbService.getMovieDetails(tmdbId);
        Movie movie = new Movie();

        // Configuración básica de la película
        movie.setTmdbId(tmdbId);
        movie.setTitle(details.getTitle());
        movie.setDurationMinutes(details.getDurationMinutes());
        movie.setDescription(details.getDescription());
        movie.setReleaseDate(details.getReleaseDate());
        movie.setVoteAverage(BigDecimal.valueOf(details.getVoteAverage()));
        movie.setVoteCount(details.getVoteCount());
        movie.setPopularity(BigDecimal.valueOf(details.getPopularity()));
        movie.setLanguage(details.getOriginalLanguage());
        movie.setImdbId(details.getImdbId());
        movie.setTrailerUrl(getTrailerUrl(details));
        movie.setIsActive(true);

        // Establecer el rating basado en vote_average
        // Usando una clasificación simple basada en el puntaje de TMDB
        if (details.getVoteAverage() != null) {
            if (details.getVoteAverage() >= 8.0) {
                movie.setRating("PG-13"); // Ejemplo - ajustar según criterios reales
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

        // Obtener el usuario actual y establecer el addedBy
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            movie.setAddedBy(username);
        }

        // Primero guardamos la película sin géneros
        Movie savedMovie = movieRepository.save(movie);

        // Procesamos los géneros
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

                    // Desconectar el género de su sesión anterior
                    entityManager.detach(genre);
                    genre = genreRepository.findById(genre.getId()).orElse(genre);

                    // Añadir género a la película sin manipular la relación bidireccional
                    savedMovie.getGenres().add(genre);

                } catch (Exception e) {
                    log.error("Error al procesar el género {}: {}", genreDto.getName(), e.getMessage());
                }
            }

            // Guardar la película con todos los géneros
            savedMovie = movieRepository.save(savedMovie);
        }

        return savedMovie;
    }

    private String getTrailerUrl(MovieDetails details) {
        // Logic to extract trailer URL from TMDB video data
        if (details.getVideos() != null && details.getVideos().getResults() != null) {
            return details.getVideos().getResults().stream()
                    .filter(video -> "Trailer".equals(video.getType()) && "YouTube".equals(video.getSite()))
                    .findFirst()
                    .map(video -> "https://www.youtube.com/watch?v=" + video.getKey())
                    .orElse(null);
        }
        return null;
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