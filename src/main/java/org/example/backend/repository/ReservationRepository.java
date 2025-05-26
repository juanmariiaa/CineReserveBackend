package org.example.backend.repository;

import org.example.backend.model.Reservation;
import org.example.backend.model.Screening;
import org.example.backend.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByScreening(Screening screening);
    List<Reservation> findByUserId(Long userId);
    
    /**
     * Encuentra todas las reservas con un estado específico creadas antes de una fecha determinada.
     * Útil para encontrar reservas pendientes que han expirado.
     * 
     * @param status El estado de las reservas a buscar
     * @param createdAt La fecha límite de creación
     * @return Lista de reservas que cumplen los criterios
     */
    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime createdAt);
}