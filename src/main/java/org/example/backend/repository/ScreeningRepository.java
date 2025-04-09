package org.example.backend.repository;

import org.example.backend.model.Movie;
import org.example.backend.model.Room;
import org.example.backend.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> findByMovie(Movie movie);
    List<Screening> findByRoom(Room room);
    List<Screening> findByDate(LocalDate date);
    List<Screening> findByDateAndMovie(LocalDate date, Movie movie);
}