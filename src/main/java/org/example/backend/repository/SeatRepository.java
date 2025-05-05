package org.example.backend.repository;

import org.example.backend.model.Room;
import org.example.backend.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRoom(Room room);
    List<Seat> findByRoomAndRowLabel(Room room, String rowLabel);
}