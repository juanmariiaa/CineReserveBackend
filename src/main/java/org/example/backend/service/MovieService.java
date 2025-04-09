package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieDetails;
import org.example.backend.model.Movie;
import org.example.backend.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final TMDBService tmdbService;

    public Movie createMovieFromTMDB(Integer tmdbId) {
        MovieDetails movieDetails = tmdbService.getMovieDetails(tmdbId);

        Movie movie = new Movie();
        movie.setTitle(movieDetails.getTitle());
        movie.setDuration(movieDetails.getDuration());
        movie.setSynopsis(movieDetails.getSynopsis());
        movie.setTmdbId(movieDetails.getId());
        movie.setImageUrl("https://image.tmdb.org/t/p/w500" + movieDetails.getImageUrl());

        return movieRepository.save(movie);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }
}