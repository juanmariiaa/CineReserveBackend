package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MovieDTO {
    private Long id;
    private String title;
    private Integer durationMinutes;
    private String description;
    private LocalDate releaseDate;
    private String posterUrl;
    private String backdropUrl;
    private String rating;
    private String language;
    private String director;
    private String trailerUrl;
    private Integer tmdbId;
    private String imdbId;
    private BigDecimal popularity;
    private BigDecimal voteAverage;
    private Integer voteCount;
    private List<String> genreNames = new ArrayList<>();
    // No incluir campos con relaciones bidireccionales
}