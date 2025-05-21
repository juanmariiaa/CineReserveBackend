package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer rows;

    @Column(nullable = false)
    private Integer columns;

    @Column(nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("room")
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("room")
    private List<Screening> screenings;

    @PrePersist
    @PreUpdate
    private void calculateCapacity() {
        this.capacity = this.rows * this.columns;
    }

    public void generateSeats() {
        if (seats == null) {
            seats = new ArrayList<>();
        } else {
            seats.clear();
        }

        for (int r = 0; r < rows; r++) {
            String rowValue = generateRowValue(r);
            for (int c = 0; c < columns; c++) {
                Seat seat = new Seat(rowValue, c + 1, this);
                seat.setPrice(new java.math.BigDecimal("10.00"));
                seats.add(seat);
            }
        }
    }

    private String generateRowValue(int rowIndex) {
        StringBuilder result = new StringBuilder();

        if (rowIndex > 25) {
            int firstChar = rowIndex / 26 - 1;
            result.append((char) ('A' + firstChar));
            rowIndex %= 26;
        }

        result.append((char) ('A' + rowIndex));
        return result.toString();
    }
}