package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningTimeDTO {
    private Long movieId;
    private String movieTitle;
    private LocalDate date;
    private List<ScreeningTimeSlot> timeSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreeningTimeSlot {
        private Long screeningId;
        private LocalTime startTime;
        private String format;
        private Boolean is3D;
        private String language;
        private Boolean hasSubtitles;
        private Integer roomNumber;
        private Integer availableSeats;
        private Double ticketPrice;
    }
}