package org.example.backend.dto;

import lombok.Data;
import org.example.backend.model.Screening;

import java.time.LocalDateTime;

@Data
public class ScreeningBasicDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Long roomId;
    private Integer roomNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean is3D;
    private Boolean hasSubtitles;
    private String language;
    private String format;
    private Integer availableSeats;
    private Integer capacity;

    public static ScreeningBasicDTO fromScreening(Screening screening) {
        ScreeningBasicDTO dto = new ScreeningBasicDTO();
        dto.setId(screening.getId());
        if (screening.getMovie() != null) {
            dto.setMovieId(screening.getMovie().getId());
            dto.setMovieTitle(screening.getMovie().getTitle());
            dto.setMoviePosterUrl(screening.getMovie().getPosterUrl());
        }
        if (screening.getRoom() != null) {
            dto.setRoomId(screening.getRoom().getId());
            dto.setRoomNumber(screening.getRoom().getNumber());
            dto.setCapacity(screening.getRoom().getCapacity());
        }
        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());
        dto.setIs3D(screening.getIs3D());
        dto.setHasSubtitles(screening.getHasSubtitles());
        dto.setLanguage(screening.getLanguage());
        dto.setFormat(screening.getFormat());
        dto.setAvailableSeats(screening.getAvailableSeats());

        return dto;
    }
}