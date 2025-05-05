package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomDTO;
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
    public ResponseEntity<Room> createRoom(@RequestBody RoomDTO roomDTO) {
        Room createdRoom = roomService.createFromDTO(roomDTO);
        return ResponseEntity.ok(createdRoom);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Room room = roomService.findById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.findAll();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<Room> getRoomByNumber(@PathVariable Integer number) {
        Room room = roomService.findByNumber(number);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/delete-highest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> deleteRoomWithHighestNumber() {
        Room deletedRoom = roomService.deleteRoomWithHighestNumber();
        return ResponseEntity.ok(deletedRoom);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}