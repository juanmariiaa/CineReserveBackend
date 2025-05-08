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

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnoreProperties({ "screenings", "hibernateLazyInitializer", "handler" })
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({ "screenings", "seats", "hibernateLazyInitializer", "handler" })
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Double ticketPrice = 8.0;

    @Column
    private Boolean is3D = false;

    @Column
    private Boolean hasSubtitles = false;

    @Column(length = 50)
    private String language = "Español";

    @Column(length = 50)
    private String format = "Digital";

    @Column(name = "is_highlighted")
    private Boolean isHighlighted = false;

    @Column(name = "booking_percentage")
    private Double bookingPercentage = 0.0;

    @Column(name = "show_type", length = 30)
    private String showType = "Regular";

    @Column(name = "has_dynamic_pricing")
    private Boolean hasDynamicPricing = false;

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL)
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

    @Transient
    public void updateBookingPercentage() {
        if (room == null) {
            this.bookingPercentage = 0.0;
            return;
        }

        int totalSeats = room.getCapacity();
        int reservedSeats = 0;

        if (reservations != null) {
            for (Reservation reservation : reservations) {
                reservedSeats += reservation.getSeatReservations().size();
            }
        }

        this.bookingPercentage = totalSeats > 0 ? (double) reservedSeats / totalSeats * 100 : 0.0;
    }
}