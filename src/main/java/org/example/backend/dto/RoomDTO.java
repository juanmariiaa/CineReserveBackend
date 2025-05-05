package org.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RoomDTO {
    @Min(value = 1, message = "Las filas deben ser al menos 1")
    @Max(value = 50, message = "Las filas no pueden exceder 50")
    private Integer rows;

    @Min(value = 1, message = "Las columnas deben ser al menos 1")
    @Max(value = 50, message = "Las columnas no pueden exceder 50")
    private Integer columns;}