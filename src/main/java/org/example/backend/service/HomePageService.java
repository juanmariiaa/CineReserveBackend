package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.MovieWithScreeningsDTO;
import org.example.backend.dto.ScreeningDTO;
import org.example.backend.model.Movie;
import org.example.backend.model.Screening;
import org.example.backend.repository.MovieRepository;
import org.example.backend.repository.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageService {
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;

    public List<ScreeningDTO> getTodaysScreenings() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);

        return screeningRepository.findByStartTimeBetweenOrderByStartTime(startOfDay, endOfDay)
                .stream()
                .map(this::convertToScreeningDTO)
                .collect(Collectors.toList());
    }

    public List<ScreeningDTO> getFeaturedScreenings() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime oneWeekLater = today.plusDays(7).atStartOfDay();

        return screeningRepository.findByIsHighlightedTrueAndStartTimeBetween(startOfDay, oneWeekLater)
                .stream()
                .map(this::convertToScreeningDTO)
                .collect(Collectors.toList());
    }

    public List<MovieWithScreeningsDTO> getFeaturedMoviesWithScreenings() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekLater = today.plusDays(7).atTime(23, 59, 59);

        List<Movie> featuredMovies = movieRepository.findByIsFeaturedTrue();

        return featuredMovies.stream()
                .map(movie -> {
                    List<Screening> screenings = screeningRepository.findByMovieIdAndStartTimeAfterAndEndTimeBefore(
                            movie.getId(), now, oneWeekLater);
                    return new MovieWithScreeningsDTO(movie, screenings);
                })
                .filter(dto -> !dto.getScreenings().isEmpty())
                .collect(Collectors.toList());
    }

    public List<MovieWithScreeningsDTO> getCurrentMoviesWithScreenings() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekLater = today.plusDays(7).atTime(23, 59, 59);

        List<Movie> activeMovies = movieRepository.findByIsActiveTrue();

        return activeMovies.stream()
                .map(movie -> {
                    List<Screening> screenings = screeningRepository.findByMovieIdAndStartTimeAfterAndEndTimeBefore(
                            movie.getId(), now, oneWeekLater);
                    return new MovieWithScreeningsDTO(movie, screenings);
                })
                .filter(dto -> !dto.getScreenings().isEmpty())
                .collect(Collectors.toList());
    }

    private ScreeningDTO convertToScreeningDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setMovieTitle(screening.getMovie().getTitle());
        dto.setMoviePosterUrl(screening.getMovie().getPosterUrl());
        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());
        dto.setRoomNumber(screening.getRoom().getNumber());
        dto.setTicketPrice(screening.getTicketPrice());
        dto.setFormat(screening.getFormat());
        dto.setLanguage(screening.getLanguage());
        dto.setHasSubtitles(screening.getHasSubtitles());
        dto.setIs3D(screening.getIs3D());
        dto.setAvailableSeats(screening.getAvailableSeats());
        dto.setIsHighlighted(screening.getIsHighlighted());
        dto.setShowType(screening.getShowType());
        dto.setBookingPercentage(screening.getBookingPercentage());
        return dto;
    }
}