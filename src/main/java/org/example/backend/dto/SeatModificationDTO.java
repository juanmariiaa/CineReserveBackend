package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatModificationDTO {
    private List<Long> seatIdsToRemove;
    private List<Long> seatIdsToAdd;
}