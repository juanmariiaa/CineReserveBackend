package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "screening")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnoreProperties({ "screenings", "hibernateLazyInitializer", "handler" })
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({ "screenings", "seats", "hibernateLazyInitializer", "handler" })
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column
    private Boolean is3D = false;

    @Column
    private Boolean hasSubtitles = false;

    @Column(length = 50)
    private String language = "Español";

    @Column(length = 50)
    private String format = "Digital";

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "screening" })
    private List<Reservation> reservations = new ArrayList<>();

    @Transient
    public boolean isActive() {
        return LocalDateTime.now().isBefore(endTime);
    }

    @Transient
    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startTime);
    }

    @Transient
    public int getAvailableSeats() {
        if (room == null)
            return 0;

        int totalSeats = room.getCapacity();
        int reservedSeats = 0;

        if (reservations != null) {
            for (Reservation reservation : reservations) {
                reservedSeats += reservation.getSeatReservations().size();
            }
        }

        return totalSeats - reservedSeats;
    }
}