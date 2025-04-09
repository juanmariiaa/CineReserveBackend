package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Screening;
import org.example.backend.model.Seat;
import org.example.backend.model.SeatReservation;
import org.example.backend.repository.SeatReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatReservationService {

    private final SeatReservationRepository seatReservationRepository;

    public SeatReservation save(SeatReservation seatReservation) {
        if (seatReservationRepository.existsByScreeningAndSeat(
                seatReservation.getScreening(),
                seatReservation.getSeat())) {
            throw new RuntimeException("Seat already reserved for this screening");
        }
        return seatReservationRepository.save(seatReservation);
    }

    public SeatReservation findById(Long id) {
        return seatReservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seat reservation not found"));
    }

    public List<SeatReservation> findByScreening(Screening screening) {
        return seatReservationRepository.findByScreening(screening);
    }

    public List<SeatReservation> findBySeat(Seat seat) {
        return seatReservationRepository.findBySeat(seat);
    }

    public List<SeatReservation> findAll() {
        return seatReservationRepository.findAll();
    }

    public void delete(Long id) {
        seatReservationRepository.deleteById(id);
    }
}