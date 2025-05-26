package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.model.Reservation;
import org.example.backend.model.enums.ReservationStatus;
import org.example.backend.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupService {

    private final ReservationRepository reservationRepository;
    
    // Tiempo máximo (en minutos) que una reserva puede estar en estado PENDING
    private static final int RESERVATION_TIMEOUT_MINUTES = 15; // 1.5 minutos

    /**
     * Tarea programada que se ejecuta cada minuto para cancelar reservas pendientes
     * que han excedido el tiempo límite.
     */
    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Iniciando limpieza de reservas expiradas");
        
        // Calcular el tiempo límite (ahora menos el tiempo de expiración)
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(RESERVATION_TIMEOUT_MINUTES);
        
        // Buscar todas las reservas en estado PENDING creadas antes del tiempo de expiración
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING, expirationTime);
        
        if (!expiredReservations.isEmpty()) {
            log.info("Se encontraron {} reservas expiradas para cancelar", expiredReservations.size());
            
            // Cambiar el estado de todas las reservas expiradas a CANCELLED
            for (Reservation reservation : expiredReservations) {
                reservation.setStatus(ReservationStatus.CANCELLED);
                log.info("Cancelando reserva expirada: ID={}, creada el {}", 
                        reservation.getId(), reservation.getCreatedAt());
            }
            
            // Guardar todas las reservas actualizadas
            reservationRepository.saveAll(expiredReservations);
            log.info("Se han cancelado {} reservas expiradas", expiredReservations.size());
        } else {
            log.debug("No se encontraron reservas expiradas");
        }
    }
}
