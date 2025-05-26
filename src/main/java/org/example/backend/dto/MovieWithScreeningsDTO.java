package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.model.Genre;
import org.example.backend.model.Movie;
import org.example.backend.model.Screening;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieWithScreeningsDTO {
    private Long id;
    private String title;
    private String posterUrl;
    private Integer durationMinutes;
    private String rating;
    private Set<String> genres;
    private Boolean isFeatured;
    private List<ScreeningDTO> screenings;
    private String ageRating;

    public MovieWithScreeningsDTO(Movie movie, List<Screening> screenings) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.posterUrl = movie.getPosterUrl();
        this.durationMinutes = movie.getDurationMinutes();
        this.rating = movie.getRating();
        this.genres = movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());
        this.isFeatured = movie.getIsFeatured();
        this.ageRating = movie.getAgeRating();
        this.screenings = screenings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ScreeningDTO convertToDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setMovieTitle(screening.getMovie().getTitle());
        dto.setMoviePosterUrl(screening.getMovie().getPosterUrl());
        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());
        dto.setRoomNumber(screening.getRoom().getNumber());
        dto.setFormat(screening.getFormat());
        dto.setLanguage(screening.getLanguage());
        dto.setHasSubtitles(screening.getHasSubtitles());
        dto.setIs3D(screening.getIs3D());
        dto.setAvailableSeats(screening.getAvailableSeats());

        // Removed default values and calculations related to isHighlighted, showType,
        // and bookingPercentage
        return dto;
    }
}