package org.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MovieSearchResponse {
    private int page;
    private List<MovieResult> results = new ArrayList<>();

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_results")
    private int totalResults;

    @Data
    @NoArgsConstructor
    public static class MovieResult {
        private boolean adult;

        @JsonProperty("backdrop_path")
        private String backdropPath;

        @JsonProperty("genre_ids")
        private List<Integer> genreIds;

        private Integer id;

        @JsonProperty("original_language")
        private String originalLanguage;

        @JsonProperty("original_title")
        private String originalTitle;

        private String overview;
        private double popularity;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("release_date")
        private LocalDate releaseDate;

        private String title;
        private boolean video;

        @JsonProperty("vote_average")
        private double voteAverage;

        @JsonProperty("vote_count")
        private int voteCount;
    }
}