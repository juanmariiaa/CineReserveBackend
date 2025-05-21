package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Reservation;
import org.example.backend.model.Seat;
import org.example.backend.model.SeatReservation;
import org.example.backend.service.ReservationService;
import org.example.backend.service.SeatService;
import org.example.backend.service.ScreeningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Seats", description = "Seat management APIs")
public class SeatController {

    private final SeatService seatService;
    private final ReservationService reservationService;
    private final ScreeningService screeningService;

    @GetMapping("/screening/{screeningId}")
    @Operation(summary = "Get seats for a screening", description = "Retrieves all seats for a specific screening with their reservation status")
    public ResponseEntity<List<Map<String, Object>>> getSeatsForScreening(@PathVariable Long screeningId) {
        // Get the screening to find the room
        var screening = screeningService.getScreeningById(screeningId);

        // Get all seats in the room
        List<Seat> roomSeats = seatService.findAll().stream()
                .filter(seat -> seat.getRoom().getId().equals(screening.getRoom().getId()))
                .collect(Collectors.toList());

        // Get all reservations for this screening
        List<Reservation> screeningReservations = reservationService.getReservationsByScreening(screeningId);

        // Get all reserved seat IDs
        List<Long> reservedSeatIds = screeningReservations.stream()
                .flatMap(reservation -> reservation.getSeatReservations().stream())
                .map(SeatReservation::getSeat)
                .map(Seat::getId)
                .collect(Collectors.toList());

        // Convert seats to response object with availability status
        List<Map<String, Object>> seatStatusList = roomSeats.stream()
                .map(seat -> {
                    boolean isReserved = reservedSeatIds.contains(seat.getId());

                    Map<String, Object> seatMap = new HashMap<>();
                    seatMap.put("id", seat.getId());
                    seatMap.put("row", seat.getRow());
                    seatMap.put("number", seat.getNumber());
                    seatMap.put("status", isReserved ? "RESERVED" : "AVAILABLE");
                    seatMap.put("roomId", seat.getRoom().getId());

                    return seatMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(seatStatusList);
    }

    @GetMapping("/screening/{screeningId}/available")
    @Operation(summary = "Get available seats for a screening", description = "Retrieves only available seats for a specific screening")
    public ResponseEntity<List<Map<String, Object>>> getAvailableSeatsForScreening(@PathVariable Long screeningId) {
        // Get all seats with status
        List<Map<String, Object>> allSeats = getSeatsForScreening(screeningId).getBody();

        // Filter to only include available seats
        if (allSeats != null) {
            List<Map<String, Object>> availableSeats = allSeats.stream()
                    .filter(seatMap -> "AVAILABLE".equals(seatMap.get("status")))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(availableSeats);
        }

        return ResponseEntity.ok(List.of());
    }
}