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

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("room")
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("room")
    private List<Screening> screenings;

    @PrePersist
    @PreUpdate
    private void calculateCapacity() {
        this.capacity = this.rows * this.columns;
    }

    /**
     * Genera asientos para esta sala de forma simétrica.
     * Las filas se etiquetan desde 'A' hasta 'Z' y luego 'AA', 'AB', etc.
     * Las columnas son numéricas comenzando desde 1.
     */
    public void generateSeats() {
        // Limpia asientos existentes si los hubiera
        if (seats == null) {
            seats = new ArrayList<>();
        } else {
            seats.clear();
        }

        // Genera nuevos asientos
        for (int r = 0; r < rows; r++) {
            String rowLabel = generateRowLabel(r);
            for (int c = 0; c < columns; c++) {
                seats.add(new Seat(rowLabel, c + 1, this));
            }
        }
    }

    /**
     * Genera una etiqueta de fila alfabética basada en el índice
     * (A, B, C, ..., Z, AA, AB, ...)
     */
    private String generateRowLabel(int rowIndex) {
        StringBuilder result = new StringBuilder();

        // Para índices mayores a 25 (después de 'Z')
        if (rowIndex > 25) {
            int firstChar = rowIndex / 26 - 1;
            result.append((char)('A' + firstChar));
            rowIndex %= 26;
        }

        result.append((char)('A' + rowIndex));
        return result.toString();
    }
}