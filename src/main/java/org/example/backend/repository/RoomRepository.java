package org.example.backend.repository;

import org.example.backend.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {


    Room findByNumber(Integer number);


    @Query("SELECT MAX(r.number) FROM Room r")
    Optional<Integer> findMaxRoomNumber();


    @Query("SELECT r FROM Room r WHERE r.number = (SELECT MAX(r2.number) FROM Room r2)")
    Optional<Room> findRoomWithHighestNumber();
}