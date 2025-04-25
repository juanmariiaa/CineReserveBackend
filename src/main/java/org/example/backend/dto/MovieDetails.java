package org.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieDetails {
    private Integer id;
    private String title;

    @JsonProperty("runtime")
    private Integer durationMinutes;

    @JsonProperty("overview")
    private String description;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("poster_path")
    private String posterUrl;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("popularity")
    private Double popularity;

    @JsonProperty("imdb_id")
    private String imdbId;

    private List<Genre> genres;

    @JsonProperty("videos")
    private Videos videos;

    // Clase interna para representar el género
    @Data
    public static class Genre {
        private Integer id;
        private String name;
    }

    // Clase interna para representar los videos (trailers, etc.)
    @Data
    public static class Videos {
        private List<Video> results;
    }

    @Data
    public static class Video {
        private String id;
        private String key;
        private String name;
        private String site;
        private String type;
    }
}