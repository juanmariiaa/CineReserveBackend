package org.example.backend.repository;

import org.example.backend.model.Reservation;
import org.example.backend.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByScreening(Screening screening);
    List<Reservation> findByUserId(Long userId);
}