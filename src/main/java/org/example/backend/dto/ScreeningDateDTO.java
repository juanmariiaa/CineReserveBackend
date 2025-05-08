package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningDateDTO {
    private Long movieId;
    private String movieTitle;
    private Set<LocalDate> availableDates;
}