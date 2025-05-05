package org.example.backend.service;

import org.example.backend.dto.MovieSearchResponse;
import org.example.backend.dto.MovieDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Collections;

@Service
public class TMDBService {
    private static final Logger log = LoggerFactory.getLogger(TMDBService.class);

    private final WebClient webClient;
    private final String accessToken;

    public TMDBService(@Value("${tmdb.api.key}") String accessToken) {
        this.accessToken = accessToken;
        log.info("Initializing TMDB Service with token length: {}", accessToken.length());

        this.webClient = WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    public MovieDetails getMovieDetails(Integer tmdbId) {
        try {
            log.info("Fetching movie details for TMDB ID: {}", tmdbId);
            return webClient.get()
                    .uri("/movie/{id}", tmdbId)
                    .retrieve()
                    .bodyToMono(MovieDetails.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error fetching movie details: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Failed to fetch movie details from TMDB", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching movie details: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch movie details from TMDB", e);
        }
    }

    public MovieSearchResponse searchMoviesByName(String query) {
        try {
            log.info("Searching for movies with query: {}", query);

            // Important: Include append_to_response for complete data
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("query", query)
                            .queryParam("include_adult", "false")
                            .queryParam("language", "en-US")
                            .queryParam("page", "1")
                            .build())
                    .retrieve()
                    .bodyToMono(MovieSearchResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error searching movies: {} - {}", e.getStatusCode(), e.getMessage());
            log.error("Response body: {}", e.getResponseBodyAsString());

            // Return an empty response object instead of throwing
            MovieSearchResponse emptyResponse = new MovieSearchResponse();
            emptyResponse.setPage(1);
            emptyResponse.setResults(Collections.emptyList());
            emptyResponse.setTotalPages(0);
            emptyResponse.setTotalResults(0);
            return emptyResponse;
        } catch (Exception e) {
            log.error("Unexpected error searching movies: {}", e.getMessage(), e);

            // Return an empty response object
            MovieSearchResponse emptyResponse = new MovieSearchResponse();
            emptyResponse.setPage(1);
            emptyResponse.setResults(Collections.emptyList());
            emptyResponse.setTotalPages(0);
            emptyResponse.setTotalResults(0);
            return emptyResponse;
        }
    }
}