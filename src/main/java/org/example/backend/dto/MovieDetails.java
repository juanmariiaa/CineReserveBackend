package org.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MovieDetails {
    private Integer id;
    private String title;
    @JsonProperty("runtime")
    private Integer duration;
    @JsonProperty("overview")
    private String synopsis;
    private List<Genre> genres;  // Cambiado de Integer[] a List<Genre>
    @JsonProperty("poster_path")
    private String imageUrl;

    // Clase interna para representar el género
    @Data
    public static class Genre {
        private Integer id;
        private String name;
    }
}