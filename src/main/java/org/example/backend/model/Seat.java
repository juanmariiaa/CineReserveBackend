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
    private String rowLabel;  // Cambiado de "row" a "rowLabel" para evitar confusión con palabras reservadas SQL

    @Column(nullable = false)
    private Integer columnNumber;  // Cambiado de "number" a "columnNumber" para mayor claridad

    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonIgnoreProperties("seats")
    private Room room;

    @OneToMany(mappedBy = "seat")
    private List<SeatReservation> seatReservations;

    // Constructor útil para crear asientos fácilmente
    public Seat(String rowLabel, Integer columnNumber, Room room) {
        this.rowLabel = rowLabel;
        this.columnNumber = columnNumber;
        this.room = room;
    }
}