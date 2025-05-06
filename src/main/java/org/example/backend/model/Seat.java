package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seat")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rowLabel;

    @Column(nullable = false)
    private Integer columnNumber;

    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonIgnoreProperties({"seats", "screenings"})
    private Room room;

    @OneToMany(mappedBy = "seat")
    @JsonIgnoreProperties({"seat"})
    private List<SeatReservation> seatReservations;

    // Constructor útil para crear asientos fácilmente
    public Seat(String rowLabel, Integer columnNumber, Room room) {
        this.rowLabel = rowLabel;
        this.columnNumber = columnNumber;
        this.room = room;
    }
}