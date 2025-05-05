package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReservationCreateDTO;
import org.example.backend.dto.SeatModificationDTO;
import org.example.backend.model.Reservation;
import org.example.backend.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody ReservationCreateDTO dto) {
        Reservation reservation = reservationService.createReservation(dto);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{reservationId}/seats")
    public ResponseEntity<Reservation> modifySeats(
            @PathVariable Long reservationId,
            @Valid @RequestBody SeatModificationDTO dto) {
        Reservation reservation = reservationService.modifySeats(reservationId, dto);
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}