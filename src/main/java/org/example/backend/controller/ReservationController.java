package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReservationCreateDTO;
import org.example.backend.dto.SeatModificationDTO;
import org.example.backend.model.Reservation;
import org.example.backend.service.ReservationService;
import org.example.backend.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Reservation", description = "Reservation management APIs")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reservations", description = "Retrieves a list of all reservations (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves a single reservation by its ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reservations by user", description = "Retrieves all reservations for a specific user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Reservation>> getReservationsByUser(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/screening/{screeningId}")
    @Operation(summary = "Get reservations by screening", description = "Retrieves all reservations for a specific screening", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Reservation>> getReservationsByScreening(@PathVariable Long screeningId) {
        List<Reservation> reservations = reservationService.getReservationsByScreening(screeningId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user's reservations", description = "Retrieves all reservations for the authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Reservation>> getCurrentUserReservations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        List<Reservation> reservations = reservationService.getReservationsByUsername(username);
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    @Operation(summary = "Create a reservation", description = "Creates a new reservation for a screening with selected seats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody ReservationCreateDTO dto) {
        Reservation reservation = reservationService.createReservation(dto);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{reservationId}/seats")
    @Operation(summary = "Modify seats in a reservation", description = "Modifies the seats associated with an existing reservation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Reservation> modifySeats(
            @PathVariable Long reservationId,
            @Valid @RequestBody SeatModificationDTO dto) {
        Reservation reservation = reservationService.modifySeats(reservationId, dto);
        return ResponseEntity.ok(reservation);
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel a reservation", description = "Cancels an existing reservation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReservationBySessionId(@PathVariable String sessionId) {
        try {
            Optional<Reservation> reservationOpt = reservationService.getReservationBySessionId(sessionId);
            
            if (reservationOpt.isPresent()) {
                return ResponseEntity.ok(reservationOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Reservation not found for session ID: " + sessionId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving reservation: " + e.getMessage()));
        }
    }
}
