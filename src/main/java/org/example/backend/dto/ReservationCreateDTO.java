package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateDTO {
    private Long userId;
    private Long screeningId;
    private List<Long> seatIds;

}