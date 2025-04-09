package org.example.backend.repository;

import org.example.backend.model.Screening;
import org.example.backend.model.Seat;
import org.example.backend.model.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    List<SeatReservation> findByScreening(Screening screening);
    List<SeatReservation> findBySeat(Seat seat);
    boolean existsByScreeningAndSeat(Screening screening, Seat seat);
}