package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomDTO;
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
        // Si el número de sala no está establecido, generarlo automáticamente
        if (room.getNumber() == null) {
            room.setNumber(generateNextRoomNumber());
        }
        return roomRepository.save(room);
    }

    /**
     * Genera el siguiente número de sala basado en el máximo actual + 1
     * @return el siguiente número de sala disponible
     */
    private Integer generateNextRoomNumber() {
        Optional<Integer> maxRoomNumber = roomRepository.findMaxRoomNumber();
        return maxRoomNumber.map(integer -> integer + 1).orElse(1); // Si no hay salas, empezar desde 1
    }

    /**
     * Crea una sala a partir de un DTO
     * @param roomDTO datos de la sala a crear
     * @return la sala creada
     */
    public Room createFromDTO(RoomDTO roomDTO) {
        Room room = new Room();
        // No establecer el número, se generará automáticamente
        room.setRows(roomDTO.getRows());
        room.setColumns(roomDTO.getColumns());
        // Guardar la sala y se generará el número automáticamente
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

    /**
     * Elimina la sala con el número más grande
     * @return la sala eliminada
     */
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

    /**
     * Método existente para eliminar por ID
     * Podría mantenerse para compatibilidad o eliminarse si no es necesario
     */
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }
}