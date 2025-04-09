package org.example.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.model.Movie;
import org.example.backend.model.Screening;
import org.example.backend.repository.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreeningService {

    private final ScreeningRepository screeningRepository;

    public Screening save(Screening screening) {
        return screeningRepository.save(screening);
    }

    public Screening findById(Long id) {
        return screeningRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Screening not found"));
    }

    public List<Screening> findByMovie(Movie movie) {
        return screeningRepository.findByMovie(movie);
    }

    public List<Screening> findByDate(LocalDate date) {
        return screeningRepository.findByDate(date);
    }

    public List<Screening> findByDateAndMovie(LocalDate date, Movie movie) {
        return screeningRepository.findByDateAndMovie(date, movie);
    }

    public List<Screening> findAll() {
        return screeningRepository.findAll();
    }

    public void delete(Long id) {
        screeningRepository.deleteById(id);
    }
}