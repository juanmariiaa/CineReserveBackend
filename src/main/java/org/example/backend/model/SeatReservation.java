package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seat_reservation")
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    @JsonIgnoreProperties({"seatReservations", "screening", "user"})
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    @JsonIgnoreProperties({"seatReservations", "room"})
    private Seat seat;
}
