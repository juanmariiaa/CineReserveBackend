package org.example.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomBasicDTO;
import org.example.backend.dto.RoomCreationDTO;
import org.example.backend.model.Room;
import org.example.backend.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomCreationDTO roomCreationDTO) {
        Room createdRoom = roomService.createFromDTO(roomCreationDTO);
        return ResponseEntity.ok(createdRoom);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Room room = roomService.findById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/basic/{id}")
    public ResponseEntity<RoomBasicDTO> getRoomBasicById(@PathVariable Long id) {
        RoomBasicDTO room = roomService.findBasicById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.findAll();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/basic")
    public ResponseEntity<List<RoomBasicDTO>> getAllRoomsBasic() {
        List<RoomBasicDTO> rooms = roomService.findAllBasic();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<Room> getRoomByNumber(@PathVariable Integer number) {
        Room room = roomService.findByNumber(number);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/delete-highest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoomWithHighestNumber() {
        roomService.deleteRoomWithHighestNumber();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}