package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ScreeningCreationDTO;
import org.example.backend.dto.ScreeningDateDTO;
import org.example.backend.dto.ScreeningTimeDTO;
import org.example.backend.exception.ErrorResponse;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.RoomNotAvailableException;
import org.example.backend.model.Screening;
import org.example.backend.service.ScreeningService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Screening", description = "Screening management APIs")
public class ScreeningController {

    private final ScreeningService screeningService;

    @GetMapping
    @Operation(summary = "Get all screenings", description = "Retrieves a list of all screenings")
    public ResponseEntity<List<Screening>> getAllScreenings() {
        List<Screening> screenings = screeningService.getAllScreenings();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get screening by ID", description = "Retrieves a single screening by its ID")
    public ResponseEntity<Screening> getScreeningById(@PathVariable Long id) {
        Screening screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a screening", description = "Creates a new screening (Admin only)")
    public ResponseEntity<Screening> createScreening(@Valid @RequestBody ScreeningCreationDTO dto) {
        Screening screening = screeningService.createScreening(dto);
        return ResponseEntity.ok(screening);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a screening", description = "Updates an existing screening (Admin only)")
    public ResponseEntity<Screening> updateScreening(
            @PathVariable Long id,
            @Valid @RequestBody ScreeningCreationDTO dto) {
        Screening screening = screeningService.updateScreening(id, dto);
        return ResponseEntity.ok(screening);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a screening", description = "Deletes a screening by its ID (Admin only)")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get screenings by movie", description = "Retrieves all screenings for a specific movie")
    public ResponseEntity<List<Screening>> getScreeningsByMovie(@PathVariable Long movieId) {
        List<Screening> screenings = screeningService.getScreeningsByMovie(movieId);
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/movie/{movieId}/dates")
    @Operation(summary = "Get available dates for a movie", description = "Retrieves all dates that have screenings for a movie")
    public ResponseEntity<ScreeningDateDTO> getAvailableDatesForMovie(@PathVariable Long movieId) {
        ScreeningDateDTO dateDTO = screeningService.getAvailableDatesForMovie(movieId);
        return ResponseEntity.ok(dateDTO);
    }

    @GetMapping("/movie/{movieId}/date/{date}")
    @Operation(summary = "Get screenings by movie and date", description = "Retrieves all screenings for a movie on a specific date")
    public ResponseEntity<ScreeningTimeDTO> getScreeningsByMovieAndDate(
            @PathVariable Long movieId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ScreeningTimeDTO timeDTO = screeningService.getScreeningsByMovieAndDate(movieId, date);
        return ResponseEntity.ok(timeDTO);
    }

    @GetMapping("/movie/{movieId}/daterange")
    @Operation(summary = "Get screenings by movie and date range", description = "Retrieves all screenings for a movie within a date range")
    public ResponseEntity<List<ScreeningTimeDTO>> getScreeningsByMovieForDateRange(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ScreeningTimeDTO> timeDTOs = screeningService.getScreeningsByMovieForDateRange(movieId, startDate,
                endDate);
        return ResponseEntity.ok(timeDTOs);
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get screenings by room", description = "Retrieves all screenings for a specific room")
    public ResponseEntity<List<Screening>> getScreeningsByRoom(@PathVariable Long roomId) {
        List<Screening> screenings = screeningService.getScreeningsByRoom(roomId);
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get screenings by date", description = "Retrieves all screenings on a specific date")
    public ResponseEntity<List<Screening>> getScreeningsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Screening> screenings = screeningService.getScreeningsByDate(date);
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/time-range")
    @Operation(summary = "Get screenings by time range", description = "Retrieves all screenings within a specific time range")
    public ResponseEntity<List<Screening>> getScreeningsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Screening> screenings = screeningService.getScreeningsByTimeRange(startTime, endTime);
        return ResponseEntity.ok(screenings);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage(),
                LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotAvailable(RoomNotAvailableException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflicto de horarios",
                ex.getMessage(),
                LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
