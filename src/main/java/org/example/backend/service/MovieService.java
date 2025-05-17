package org.example.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieDetails;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Genre;
import org.example.backend.model.Movie;
import org.example.backend.model.Screening;
import org.example.backend.repository.GenreRepository;
import org.example.backend.repository.MovieRepository;
import org.example.backend.repository.ScreeningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    private final ScreeningRepository screeningRepository;

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

    public List<Movie> getFilteredMovies(
            String searchTerm,
            List<String> genres,
            String duration,
            String rating,
            String timeFrame,
            String sortBy) {
        
        log.info("Filtering movies with parameters: searchTerm={}, genres={}, duration={}, rating={}, timeFrame={}, sortBy={}",
                searchTerm, genres, duration, rating, timeFrame, sortBy);
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> query = cb.createQuery(Movie.class);
        Root<Movie> movie = query.from(Movie.class);
        
        // Always filter for active movies
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(movie.get("isActive"), true));
        
        // Filter by search term if provided
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(movie.get("title")), searchPattern));
        }
        
        // Filter by genres if provided
        if (genres != null && !genres.isEmpty()) {
            Join<Movie, Genre> genreJoin = movie.join("genres");
            predicates.add(genreJoin.get("name").in(genres));
        }
        
        // Filter by duration
        if (duration != null && !duration.isEmpty()) {
            switch (duration) {
                case "short":
                    predicates.add(cb.lessThan(movie.get("durationMinutes"), 90));
                    break;
                case "medium":
                    predicates.add(cb.between(movie.get("durationMinutes"), 90, 120));
                    break;
                case "long":
                    predicates.add(cb.greaterThan(movie.get("durationMinutes"), 120));
                    break;
            }
        }
        
        // Filter by rating
        if (rating != null && !rating.isEmpty()) {
            predicates.add(cb.equal(movie.get("rating"), rating));
        }
        
        // Filter by time frame (movies with screenings in the specified time period)
        if (timeFrame != null && !timeFrame.isEmpty()) {
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime;
            
            switch (timeFrame) {
                case "today":
                    endTime = startTime.toLocalDate().atTime(23, 59, 59);
                    break;
                case "week":
                    endTime = startTime.plusDays(7);
                    break;
                default:
                    endTime = startTime.plusYears(1); // Far future for "all"
                    break;
            }
            
            // Get movie IDs with screenings in the time range
            List<Long> movieIdsWithScreenings = screeningRepository.findMovieIdsWithScreeningsBetween(startTime, endTime);
            
            if (!movieIdsWithScreenings.isEmpty()) {
                predicates.add(movie.get("id").in(movieIdsWithScreenings));
            }
        }
        
        // Apply predicates to query
        query.where(predicates.toArray(new Predicate[0]));
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "title-asc":
                    query.orderBy(cb.asc(movie.get("title")));
                    break;
                case "title-desc":
                    query.orderBy(cb.desc(movie.get("title")));
                    break;
                case "rating":
                    query.orderBy(cb.desc(movie.get("voteAverage")));
                    break;
                default:
                    query.orderBy(cb.asc(movie.get("title")));
                    break;
            }
        } else {
            // Default sort by title
            query.orderBy(cb.asc(movie.get("title")));
        }
        
        // Execute query
        TypedQuery<Movie> typedQuery = entityManager.createQuery(query);
        List<Movie> filteredMovies = typedQuery.getResultList();
        
        log.info("Found {} movies after filtering", filteredMovies.size());
        
        return filteredMovies;
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