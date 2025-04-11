package org.example.backend.service;

import org.example.backend.dto.MovieDetails;
import org.example.backend.dto.MovieSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class TMDBService {
    private final WebClient webClient;
    private final String accessToken; // Renombrado para claridad

    public TMDBService(@Value("${tmdb.api.key}") String accessToken) {
        this.accessToken = accessToken;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    public MovieDetails getMovieDetails(Integer tmdbId) {
        return webClient.get()
                .uri("/movie/{id}", tmdbId)
                .retrieve()
                .bodyToMono(MovieDetails.class)
                .block();
    }

    public MovieSearchResponse searchMoviesByName(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToMono(MovieSearchResponse.class)
                .block();
    }


}