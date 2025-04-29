package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieDetails;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Genre;
import org.example.backend.model.Movie;
import org.example.backend.repository.GenreRepository;
import org.example.backend.repository.MovieRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final TMDBService tmdbService;
    private final GenreRepository genreRepository;

    public Movie createMovieFromTMDB(Integer tmdbId) {
        MovieDetails details = tmdbService.getMovieDetails(tmdbId);
        Movie movie = new Movie();

        movie.setTmdbId(tmdbId);
        movie.setTitle(details.getTitle());
        // Update these method calls to match the new field names in MovieDetails
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


        // Set poster and backdrop URLs - update to use the new field names
        if (details.getPosterUrl() != null) {
            movie.setPosterUrl("https://image.tmdb.org/t/p/w500" + details.getPosterUrl());
        }
        if (details.getBackdropPath() != null) {
            movie.setBackdropUrl("https://image.tmdb.org/t/p/original" + details.getBackdropPath());
        }

        // Set creation timestamps
        LocalDateTime now = LocalDateTime.now();
        movie.setCreatedAt(now);
        movie.setUpdatedAt(now);

        // Add genres
        Set<Genre> genres = new HashSet<>();
        if (details.getGenres() != null) {
            for (MovieDetails.Genre genreDto : details.getGenres()) {
                Genre genre = genreRepository.findByTmdbGenreId(genreDto.getId())
                        .orElseGet(() -> {
                            Genre newGenre = new Genre();
                            newGenre.setName(genreDto.getName());
                            newGenre.setTmdbGenreId(genreDto.getId());
                            return genreRepository.save(newGenre);
                        });
                genres.add(genre);
            }
        }
        movie.setGenres(genres);

        return movieRepository.save(movie);
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