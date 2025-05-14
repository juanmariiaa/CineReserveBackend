package org.example.backend.dto;

import lombok.Data;
import org.example.backend.model.Room;

@Data
public class RoomBasicDTO {
    private Long id;
    private Integer number;
    private Integer capacity;
    private Integer rows;
    private Integer columns;

    public static RoomBasicDTO fromRoom(Room room) {
        RoomBasicDTO dto = new RoomBasicDTO();
        dto.setId(room.getId());
        dto.setNumber(room.getNumber());
        dto.setCapacity(room.getCapacity());
        dto.setRows(room.getRows());
        dto.setColumns(room.getColumns());
        return dto;
    }
}