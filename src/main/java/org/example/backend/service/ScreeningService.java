package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ScreeningCreationDTO;
import org.example.backend.dto.ScreeningDateDTO;
import org.example.backend.dto.ScreeningTimeDTO;
import org.example.backend.dto.ScreeningTimeDTO.ScreeningTimeSlot;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.exception.RoomNotAvailableException;
import org.example.backend.model.Movie;
import org.example.backend.model.Room;
import org.example.backend.model.Screening;
import org.example.backend.repository.MovieRepository;
import org.example.backend.repository.RoomRepository;
import org.example.backend.repository.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    public Screening createScreening(ScreeningCreationDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + dto.getMovieId()));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + dto.getRoomId()));

        LocalDateTime endTime = calculateEndTime(dto.getStartTime(), movie.getDurationMinutes());

        boolean isRoomAvailable = checkRoomAvailability(room, dto.getStartTime(), endTime);
        if (!isRoomAvailable) {
            throw new RoomNotAvailableException("The room is not available at the specified time");
        }

        LocalDateTime cleanupEndTime = endTime.plusMinutes(15);

        if (!checkCleanupTime(room, endTime, cleanupEndTime)) {
            throw new RoomNotAvailableException(
                    "There is not enough time to prepare the room before the next screening");
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

    private LocalDateTime calculateEndTime(LocalDateTime startTime, Integer durationMinutes) {
        return startTime.plusMinutes(durationMinutes + 15);
    }

    private boolean checkRoomAvailability(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        List<Screening> overlappingScreenings = screeningRepository.findOverlappingScreenings(
                room.getId(), startTime, endTime);

        return overlappingScreenings.isEmpty();
    }

    private boolean checkCleanupTime(Room room, LocalDateTime currentEndTime, LocalDateTime cleanupEndTime) {
        List<Screening> nextScreenings = screeningRepository.findNextScreeningsAfter(
                room.getId(), currentEndTime);

        if (nextScreenings.isEmpty()) {
            return true;
        }

        Screening nextScreening = nextScreenings.get(0);
        return nextScreening.getStartTime().isAfter(cleanupEndTime);
    }

    public List<Screening> getAllScreenings() {
        return screeningRepository.findAll();
    }

    public Screening getScreeningById(Long id) {
        return screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with ID: " + id));
    }

    public Screening updateScreening(Long id, ScreeningCreationDTO dto) {
        Screening screening = getScreeningById(id);

        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + dto.getMovieId()));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + dto.getRoomId()));

        LocalDateTime endTime = calculateEndTime(dto.getStartTime(), movie.getDurationMinutes());

        // Check if the room is available for the new time, excluding this screening
        boolean isRoomAvailable = checkRoomAvailabilityForUpdate(room, dto.getStartTime(), endTime, id);
        if (!isRoomAvailable) {
            throw new RoomNotAvailableException("The room is not available at the specified time");
        }

        LocalDateTime cleanupEndTime = endTime.plusMinutes(15);

        if (!checkCleanupTimeForUpdate(room, endTime, cleanupEndTime, id)) {
            throw new RoomNotAvailableException(
                    "There is not enough time to prepare the room before the next screening");
        }

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

    public void deleteScreening(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screening not found with ID: " + id);
        }
        screeningRepository.deleteById(id);
    }

    public List<Screening> getScreeningsByMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with ID: " + movieId);
        }
        return screeningRepository.findByMovieIdAndDateRange(
                movieId,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(3));
    }

    public List<Screening> getScreeningsByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with ID: " + roomId);
        }

        // Custom query to find screenings by room ID
        return screeningRepository.findAll().stream()
                .filter(screening -> screening.getRoom().getId().equals(roomId))
                .toList();
    }

    public List<Screening> getScreeningsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Custom query to find screenings by date
        return screeningRepository.findAll().stream()
                .filter(screening -> screening.getStartTime().isAfter(startOfDay) &&
                        screening.getStartTime().isBefore(endOfDay))
                .toList();
    }

    public ScreeningDateDTO getAvailableDatesForMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(1); // Show screenings for the next month

        List<Screening> screenings = screeningRepository.findByMovieIdAndDateRange(movieId, now, endDate);

        // Extract unique dates with screenings and sort them
        Set<LocalDate> availableDates = screenings.stream()
                .map(screening -> screening.getStartTime().toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new)); // TreeSet keeps elements sorted

        return new ScreeningDateDTO(movieId, movie.getTitle(), availableDates);
    }

    public ScreeningTimeDTO getScreeningsByMovieAndDate(Long movieId, LocalDate date) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Screening> screenings = screeningRepository.findByMovieIdAndStartTimeBetween(movieId, startOfDay,
                endOfDay);

        List<ScreeningTimeSlot> timeSlots = screenings.stream()
                .map(this::convertToTimeSlot)
                .sorted(Comparator.comparing(ScreeningTimeSlot::getStartTime))
                .collect(Collectors.toList());

        return new ScreeningTimeDTO(movieId, movie.getTitle(), date, timeSlots);
    }

    public List<ScreeningTimeDTO> getScreeningsByMovieForDateRange(Long movieId, LocalDate startDate,
            LocalDate endDate) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Screening> screenings = screeningRepository.findByMovieIdAndDateRange(movieId, startDateTime, endDateTime);

        // Group screenings by date
        return screenings.stream()
                .collect(Collectors.groupingBy(screening -> screening.getStartTime().toLocalDate()))
                .entrySet().stream()
                .map(entry -> {
                    List<ScreeningTimeSlot> timeSlots = entry.getValue().stream()
                            .map(this::convertToTimeSlot)
                            .sorted(Comparator.comparing(ScreeningTimeSlot::getStartTime))
                            .collect(Collectors.toList());

                    return new ScreeningTimeDTO(movieId, movie.getTitle(), entry.getKey(), timeSlots);
                })
                .sorted(Comparator.comparing(ScreeningTimeDTO::getDate))
                .collect(Collectors.toList());
    }

    private ScreeningTimeSlot convertToTimeSlot(Screening screening) {
        ScreeningTimeSlot slot = new ScreeningTimeSlot();
        slot.setScreeningId(screening.getId());
        slot.setStartTime(screening.getStartTime().toLocalTime());
        slot.setFormat(screening.getFormat());
        slot.setIs3D(screening.getIs3D());
        slot.setLanguage(screening.getLanguage());
        slot.setHasSubtitles(screening.getHasSubtitles());
        slot.setRoomNumber(screening.getRoom().getNumber());
        slot.setAvailableSeats(screening.getAvailableSeats());
        slot.setTicketPrice(screening.getTicketPrice());
        return slot;
    }

    private boolean checkRoomAvailabilityForUpdate(Room room, LocalDateTime startTime, LocalDateTime endTime,
            Long screeningId) {
        List<Screening> overlappingScreenings = screeningRepository.findOverlappingScreenings(
                room.getId(), startTime, endTime);

        // Filter out the current screening
        return overlappingScreenings.stream()
                .noneMatch(screening -> !screening.getId().equals(screeningId));
    }

    private boolean checkCleanupTimeForUpdate(Room room, LocalDateTime currentEndTime, LocalDateTime cleanupEndTime,
            Long screeningId) {
        List<Screening> nextScreenings = screeningRepository.findNextScreeningsAfter(
                room.getId(), currentEndTime);

        if (nextScreenings.isEmpty()) {
            return true;
        }

        // Filter out the current screening
        nextScreenings = nextScreenings.stream()
                .filter(screening -> !screening.getId().equals(screeningId))
                .toList();

        if (nextScreenings.isEmpty()) {
            return true;
        }

        Screening nextScreening = nextScreenings.get(0);
        return nextScreening.getStartTime().isAfter(cleanupEndTime);
    }
}
