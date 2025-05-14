package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningDTO {
    private Long id;
    private String movieTitle;
    private String moviePosterUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer roomNumber;
    private Double ticketPrice;
    private String format;
    private String language;
    private Boolean hasSubtitles;
    private Boolean is3D;
    private Integer availableSeats;
}