package org.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RoomCreationDTO {
    @Min(value = 1, message = "Rows must be at least 1")
    @Max(value = 50, message = "Rows cannot exceed 50")
    private Integer rows;

    @Min(value = 1, message = "Columns must be at least 1")
    @Max(value = 50, message = "Columns cannot exceed 50")
    private Integer columns;
}