package org.example.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScreeningCreationDTO {
    @NotNull(message = "El ID de la película es obligatorio")
    private Long movieId;

    @NotNull(message = "El ID de la sala es obligatorio")
    private Long roomId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha de proyección debe ser en el futuro")
    private LocalDateTime startTime;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double ticketPrice;

    // Campos adicionales que podrían ser útiles
    private Boolean is3D = false;
    private Boolean hasSubtitles = false;
    private String language = "Español"; // Idioma por defecto
    private String format = "Digital"; // Formato por defecto
}