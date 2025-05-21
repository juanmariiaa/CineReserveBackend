package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private String row;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 10.00")
    private BigDecimal price = new BigDecimal("10.00");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({ "seats", "hibernateLazyInitializer", "handler" })
    private Room room;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("seat")
    private List<SeatReservation> seatReservations = new ArrayList<>();

    public Seat(String row, Integer number, Room room) {
        this.row = row;
        this.number = number;
        this.room = room;
        this.price = new java.math.BigDecimal("10.00");
    }

    public Seat(String row, Integer number, Room room, java.math.BigDecimal price) {
        this.row = row;
        this.number = number;
        this.room = room;
        this.price = price;
    }
}