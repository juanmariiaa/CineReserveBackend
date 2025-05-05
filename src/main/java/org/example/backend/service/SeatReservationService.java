package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Screening;
import org.example.backend.model.Seat;
import org.example.backend.model.SeatReservation;
import org.example.backend.repository.SeatReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatReservationService {

    private final SeatReservationRepository seatReservationRepository;

}