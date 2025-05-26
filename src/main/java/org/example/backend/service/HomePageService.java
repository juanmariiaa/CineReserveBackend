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

                return screeningRepository.findAll().stream()
                                .filter(screening -> screening.getStartTime().isAfter(startOfDay) &&
                                                screening.getStartTime().isBefore(endOfDay))
                                .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                                .map(this::convertToScreeningDTO)
                                .collect(Collectors.toList());
        }

        public List<ScreeningDTO> getFeaturedScreenings() {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime oneWeekLater = now.plusDays(7);

                // Get screenings for featured movies in the next week
                List<Movie> featuredMovies = movieRepository.findByIsFeaturedTrue();
                List<Long> featuredMovieIds = featuredMovies.stream()
                                .map(Movie::getId)
                                .collect(Collectors.toList());

                return screeningRepository.findAll().stream()
                                .filter(screening -> featuredMovieIds.contains(screening.getMovie().getId()) &&
                                                screening.getStartTime().isAfter(now) &&
                                                screening.getStartTime().isBefore(oneWeekLater))
                                .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                                .limit(10) // Limit to the first 10 screenings
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
                                        List<Screening> screenings = screeningRepository.findAll().stream()
                                                        .filter(screening -> screening.getMovie().getId()
                                                                        .equals(movie.getId()) &&
                                                                        screening.getStartTime().isAfter(now) &&
                                                                        screening.getStartTime().isBefore(oneWeekLater))
                                                        .collect(Collectors.toList());
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
                                        List<Screening> screenings = screeningRepository.findAll().stream()
                                                        .filter(screening -> screening.getMovie().getId()
                                                                        .equals(movie.getId()) &&
                                                                        screening.getStartTime().isAfter(now) &&
                                                                        screening.getStartTime().isBefore(oneWeekLater))
                                                        .collect(Collectors.toList());
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
                dto.setFormat(screening.getFormat());
                dto.setLanguage(screening.getLanguage());
                dto.setHasSubtitles(screening.getHasSubtitles());
                dto.setIs3D(screening.getIs3D());
                dto.setAvailableSeats(screening.getAvailableSeats());
                return dto;
        }
}