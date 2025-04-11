package org.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MovieSearchResponse {
    private Integer page;
    private List<MovieResult> results;
    @JsonProperty("total_pages")
    private Integer totalPages;
    @JsonProperty("total_results")
    private Integer totalResults;

    @Data
    public static class MovieResult {
        private Integer id;
        private String title;
        @JsonProperty("poster_path")
        private String posterPath;
        @JsonProperty("release_date")
        private String releaseDate;
        private String overview;
    }
}