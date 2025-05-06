package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomCreationDTO;
import org.example.backend.model.Room;
import org.example.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    public Room save(Room room) {
        if (room.getNumber() == null) {
            room.setNumber(generateNextRoomNumber());
        }

        if (room.getId() == null) {
            room.generateSeats();
        }

        return roomRepository.save(room);
    }


    private Integer generateNextRoomNumber() {
        Optional<Integer> maxRoomNumber = roomRepository.findMaxRoomNumber();
        return maxRoomNumber.map(integer -> integer + 1).orElse(1);
    }


    public Room createFromDTO(RoomCreationDTO roomCreationDTO) {
        Room room = new Room();
        room.setRows(roomCreationDTO.getRows());
        room.setColumns(roomCreationDTO.getColumns());
        return save(room);
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findByNumber(Integer number) {
        return roomRepository.findByNumber(number);
    }


    public Room deleteRoomWithHighestNumber() {
        Optional<Room> roomToDelete = roomRepository.findRoomWithHighestNumber();
        if (roomToDelete.isPresent()) {
            Room room = roomToDelete.get();
            roomRepository.deleteById(room.getId());
            return room;
        } else {
            throw new RuntimeException("No rooms available to delete");
        }
    }


    public void delete(Long id) {
        roomRepository.deleteById(id);
    }
}