package org.example.backend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReservationCreateDTO;
import org.example.backend.dto.SeatModificationDTO;
import org.example.backend.exception.BusinessException;
import org.example.backend.model.*;
import org.example.backend.model.enums.ReservationStatus;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final UserRepository userRepository;

    public Reservation createReservation(ReservationCreateDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Screening screening = screeningRepository.findById(dto.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("Screening not found"));

        if (!screening.isActive()) {
            throw new BusinessException("The screening is no longer available");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);

        for (Long seatId : dto.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

            if (isSeatAlreadyReserved(screening, seat)) {
                throw new BusinessException("The seat " + seat.getRowLabel() + seat.getColumnNumber() + " is already reserved");
            }

            reservation.addSeatReservation(seat);
        }

        return reservationRepository.save(reservation);
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (reservation.getScreening().hasStarted()) {
            throw new BusinessException("Cannot cancel a reservation for a screening that has already started");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);
    }

    // Method to change seats of an existing reservation
    public Reservation modifySeats(Long reservationId, SeatModificationDTO dto) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        if (reservation.getScreening().hasStarted()) {
            throw new BusinessException("Cannot modify a reservation for a screening that has already started");
        }

        if (dto.getSeatIdsToRemove() != null && !dto.getSeatIdsToRemove().isEmpty()) {
            List<SeatReservation> toRemove = reservation.getSeatReservations().stream()
                    .filter(sr -> dto.getSeatIdsToRemove().contains(sr.getSeat().getId()))
                    .collect(Collectors.toList());

            for (SeatReservation sr : toRemove) {
                reservation.removeSeatReservation(sr);
            }
        }

        if (dto.getSeatIdsToAdd() != null && !dto.getSeatIdsToAdd().isEmpty()) {
            for (Long seatId : dto.getSeatIdsToAdd()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new EntityNotFoundException("Seat not found"));

                if (isSeatAlreadyReserved(reservation.getScreening(), seat)) {
                    throw new BusinessException("The seat " + seat.getRowLabel() + seat.getColumnNumber() + " is already reserved");
                }

                reservation.addSeatReservation(seat);
            }
        }

        return reservationRepository.save(reservation);
    }

    private boolean isSeatAlreadyReserved(Screening screening, Seat seat) {
        return seatReservationRepository.findBySeat(seat).stream()
                .anyMatch(sr ->
                        sr.getReservation().getScreening().getId().equals(screening.getId()) &&
                                !ReservationStatus.CANCELLED.equals(sr.getReservation().getStatus())
                );
    }
}