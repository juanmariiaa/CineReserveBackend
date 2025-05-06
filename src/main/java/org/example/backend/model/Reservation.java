package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.model.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"reservations", "password", "roles", "hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    @JsonIgnoreProperties({"reservations", "hibernateLazyInitializer", "handler"})
    private Screening screening;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("reservation")
    private List<SeatReservation> seatReservations = new ArrayList<>();

    public void addSeatReservation(Seat seat) {
        SeatReservation seatReservation = new SeatReservation();
        seatReservation.setReservation(this);
        seatReservation.setSeat(seat);
        this.seatReservations.add(seatReservation);
    }

    public void removeSeatReservation(SeatReservation seatReservation) {
        this.seatReservations.remove(seatReservation);
        seatReservation.setReservation(null);
        seatReservation.setSeat(null);
    }
}
