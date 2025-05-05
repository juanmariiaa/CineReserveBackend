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
        // Validaciones y obtención de entidades
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Screening screening = screeningRepository.findById(dto.getScreeningId())
                .orElseThrow(() -> new EntityNotFoundException("Proyección no encontrada"));

        // Validar si la proyección sigue activa
        if (!screening.isActive()) {
            throw new BusinessException("La proyección ya no está disponible");
        }

        // Crear la reserva
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);

        // Añadir los asientos seleccionados usando el método helper
        for (Long seatId : dto.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado"));

            // Verificar si el asiento ya está reservado para esta proyección
            if (isSeatAlreadyReserved(screening, seat)) {
                throw new BusinessException("El asiento " + seat.getRowLabel() + seat.getColumnNumber() + " ya está reservado");
            }

            // Usar el método helper para añadir el asiento a la reserva
            reservation.addSeatReservation(seat);
        }

        // Guardar la reserva
        return reservationRepository.save(reservation);
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        // Verificar si se puede cancelar
        if (reservation.getScreening().hasStarted()) {
            throw new BusinessException("No se puede cancelar una reserva para una proyección que ya ha comenzado");
        }

        // Si es una cancelación completa
        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);
    }

    // Método para cambiar asientos de una reserva existente
    public Reservation modifySeats(Long reservationId, SeatModificationDTO dto) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        // Verificar si se puede modificar
        if (reservation.getScreening().hasStarted()) {
            throw new BusinessException("No se puede modificar una reserva para una proyección que ya ha comenzado");
        }

        // Quitar asientos seleccionados
        if (dto.getSeatIdsToRemove() != null && !dto.getSeatIdsToRemove().isEmpty()) {
            List<SeatReservation> toRemove = reservation.getSeatReservations().stream()
                    .filter(sr -> dto.getSeatIdsToRemove().contains(sr.getSeat().getId()))
                    .collect(Collectors.toList());

            for (SeatReservation sr : toRemove) {
                // Usar el método helper para quitar el asiento de la reserva
                reservation.removeSeatReservation(sr);
            }
        }

        // Añadir nuevos asientos
        if (dto.getSeatIdsToAdd() != null && !dto.getSeatIdsToAdd().isEmpty()) {
            for (Long seatId : dto.getSeatIdsToAdd()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new EntityNotFoundException("Asiento no encontrado"));

                // Verificar si el asiento ya está reservado para esta proyección
                if (isSeatAlreadyReserved(reservation.getScreening(), seat)) {
                    throw new BusinessException("El asiento " + seat.getRowLabel() + seat.getColumnNumber() + " ya está reservado");
                }

                // Usar el método helper para añadir el asiento a la reserva
                reservation.addSeatReservation(seat);
            }
        }

        return reservationRepository.save(reservation);
    }

    private boolean isSeatAlreadyReserved(Screening screening, Seat seat) {
        // Implementación alternativa usando consulta JPQL personalizada
        // Verificar si existe alguna reserva activa para este asiento en esta proyección
        return seatReservationRepository.findBySeat(seat).stream()
                .anyMatch(sr ->
                        sr.getReservation().getScreening().getId().equals(screening.getId()) &&
                                !ReservationStatus.CANCELLED.equals(sr.getReservation().getStatus())
                );
    }
}