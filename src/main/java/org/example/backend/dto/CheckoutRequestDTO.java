package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    private Long screeningId;
    private List<Long> seatIds;
    private String successUrlDomain;

    /**
     * Convert this DTO to a ReservationCreateDTO
     * 
     * @return A ReservationCreateDTO with the screeningId and seatIds from this DTO
     */
    public ReservationCreateDTO getReservationCreateDTO() {
        ReservationCreateDTO dto = new ReservationCreateDTO();
        dto.setScreeningId(this.screeningId);
        dto.setSeatIds(this.seatIds);
        // userId will be null, which will make the ReservationService use the
        // authenticated user
        return dto;
    }
}