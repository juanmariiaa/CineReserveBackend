package org.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MovieDetails {
    private Integer id;
    private String title;
    @JsonProperty("runtime")
    private Integer duration;
    @JsonProperty("overview")
    private String synopsis;
    @JsonProperty("genre_ids")
    private Integer[] genres;
    @JsonProperty("poster_path")
    private String imageUrl;
}