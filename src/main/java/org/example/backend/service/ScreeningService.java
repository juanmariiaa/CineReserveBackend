package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ScreeningCreationDTO;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.RoomNotAvailableException;
import org.example.backend.model.Movie;
import org.example.backend.model.Room;
import org.example.backend.model.Screening;
import org.example.backend.repository.MovieRepository;
import org.example.backend.repository.RoomRepository;
import org.example.backend.repository.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;


    public Screening createScreening(ScreeningCreationDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada con ID: " + dto.getMovieId()));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con ID: " + dto.getRoomId()));

        LocalDateTime endTime = calculateEndTime(dto.getStartTime(), movie.getDurationMinutes());

        boolean isRoomAvailable = checkRoomAvailability(room, dto.getStartTime(), endTime);
        if (!isRoomAvailable) {
            throw new RoomNotAvailableException("La sala no está disponible en el horario especificado");
        }

        LocalDateTime cleanupEndTime = endTime.plusMinutes(15);

        if (!checkCleanupTime(room, endTime, cleanupEndTime)) {
            throw new RoomNotAvailableException("No hay tiempo suficiente para preparar la sala antes de la siguiente proyección");
        }

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setRoom(room);
        screening.setStartTime(dto.getStartTime());
        screening.setEndTime(endTime);
        screening.setTicketPrice(dto.getTicketPrice());

        screening.setIs3D(dto.getIs3D());
        screening.setHasSubtitles(dto.getHasSubtitles());
        screening.setLanguage(dto.getLanguage());
        screening.setFormat(dto.getFormat());

        return screeningRepository.save(screening);
    }

    /**
     * Calcula la hora de fin basada en la hora de inicio y la duración
     */
    private LocalDateTime calculateEndTime(LocalDateTime startTime, Integer durationMinutes) {
        // Añadir 15 minutos para anuncios y trailers
        return startTime.plusMinutes(durationMinutes + 15);
    }

    /**
     * Verifica si la sala está disponible en el horario especificado
     */
    private boolean checkRoomAvailability(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        List<Screening> overlappingScreenings = screeningRepository.findOverlappingScreenings(
                room.getId(), startTime, endTime);

        return overlappingScreenings.isEmpty();
    }

    /**
     * Verifica si hay tiempo suficiente para la preparación entre proyecciones
     */
    private boolean checkCleanupTime(Room room, LocalDateTime currentEndTime, LocalDateTime cleanupEndTime) {
        List<Screening> nextScreenings = screeningRepository.findNextScreeningsAfter(
                room.getId(), currentEndTime);

        if (nextScreenings.isEmpty()) {
            return true;  // No hay más proyecciones después
        }

        // Comprobar si la primera proyección siguiente comienza después del tiempo de limpieza
        Screening nextScreening = nextScreenings.get(0);
        return nextScreening.getStartTime().isAfter(cleanupEndTime);
    }
}