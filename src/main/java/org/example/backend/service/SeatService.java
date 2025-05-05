package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Room;
import org.example.backend.model.Seat;
import org.example.backend.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {

    private final SeatRepository seatRepository;

    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }

    public Seat findById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
    }

    public List<Seat> findByRoomAndRowLabel(Room room, String rowLabel) {
        return seatRepository.findByRoomAndRowLabel(room, rowLabel);
    }
    public List<Seat> findAll() {
        return seatRepository.findAll();
    }

    public void delete(Long id) {
        seatRepository.deleteById(id);
    }
}