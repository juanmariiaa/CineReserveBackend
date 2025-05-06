package org.example.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScreeningCreationDTO {
    @NotNull(message = "The movie ID is required")
    private Long movieId;

    @NotNull(message = "The room ID is required")
    private Long roomId;

    @NotNull(message = "The start date and time is required")
    @Future(message = "The screening date must be in the future")
    private LocalDateTime startTime;

    @Min(value = 0, message = "The price cannot be negative")
    private Double ticketPrice;

    private Boolean is3D = false;
    private Boolean hasSubtitles = false;
    private String language = "English";
    private String format = "Digital";
}