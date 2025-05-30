package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomBasicDTO;
import org.example.backend.dto.RoomCreationDTO;
import org.example.backend.model.Room;
import org.example.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // New method to get all rooms with basic info only
    public List<RoomBasicDTO> findAllBasic() {
        return roomRepository.findAll().stream()
                .map(RoomBasicDTO::fromRoom)
                .collect(Collectors.toList());
    }

    // New method to get a room with basic info by id
    public RoomBasicDTO findBasicById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return RoomBasicDTO.fromRoom(room);
    }

    public Room findByNumber(Integer number) {
        return roomRepository.findByNumber(number);
    }

    public void deleteRoomWithHighestNumber() {
        Room room = roomRepository.findRoomWithHighestNumber()
                .orElseThrow(() -> new NoSuchElementException("No rooms found"));
        roomRepository.delete(room);
    }

    public void delete(Long id) {
        roomRepository.deleteById(id);
    }
}