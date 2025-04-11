package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieDetails;
import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.model.Movie;
import org.example.backend.repository.MovieRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final TMDBService tmdbService;

    public Movie createMovieFromTMDB(Integer tmdbId) {
        MovieDetails details = tmdbService.getMovieDetails(tmdbId);
        Movie movie = new Movie();

        movie.setTmdbId(tmdbId);
        movie.setTitle(details.getTitle());
        movie.setDuration(details.getDuration());
        movie.setSynopsis(details.getSynopsis());

        String genres = details.getGenres().stream()
                .map(MovieDetails.Genre::getName)
                .collect(Collectors.joining(", "));
        movie.setGenre(genres);

        if (details.getImageUrl() != null) {
            movie.setImageUrl("https://image.tmdb.org/t/p/original" + details.getImageUrl());
        }

        return movieRepository.save(movie);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    public List<MovieSearchResponse.MovieResult> findMoviesByName(String name) {
        MovieSearchResponse response = tmdbService.searchMoviesByName(name);
        return response.getResults();
    }

    public Movie deleteMovieById(Long id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
        return movie;
    }
}