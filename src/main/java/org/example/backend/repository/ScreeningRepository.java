package org.example.backend.repository;

import org.example.backend.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

        // Special query to get all screenings with fully loaded relationships
        @Query("SELECT s FROM Screening s JOIN FETCH s.room JOIN FETCH s.movie")
        List<Screening> findAllWithRoomAndMovie();

        // Find a specific screening with fully loaded relationships
        @Query("SELECT s FROM Screening s JOIN FETCH s.room JOIN FETCH s.movie WHERE s.id = :id")
        Optional<Screening> findByIdWithRoomAndMovie(@Param("id") Long id);

        @Query("SELECT s FROM Screening s WHERE s.room.id = :roomId AND " +
                        "((s.startTime < :endTime AND s.endTime > :startTime))")
        List<Screening> findOverlappingScreenings(
                        @Param("roomId") Long roomId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Fixed query to work with PostgreSQL - use CAST instead of FUNCTION
        @Query("SELECT s FROM Screening s WHERE s.room.id = :roomId AND " +
                        "CAST(s.startTime AS date) = CAST(:date AS date)")
        List<Screening> findByRoomIdAndDate(
                        @Param("roomId") Long roomId,
                        @Param("date") LocalDateTime date);

        @Query("SELECT s FROM Screening s WHERE s.room.id = :roomId AND " +
                        "s.startTime > :afterTime ORDER BY s.startTime ASC")
        List<Screening> findNextScreeningsAfter(
                        @Param("roomId") Long roomId,
                        @Param("afterTime") LocalDateTime afterTime);

        @Query("SELECT s FROM Screening s WHERE s.endTime > :now ORDER BY s.startTime")
        List<Screening> findActiveScreenings(@Param("now") LocalDateTime now);

        @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND " +
                        "s.startTime BETWEEN :fromDate AND :toDate ORDER BY s.startTime")
        List<Screening> findByMovieIdAndDateRange(
                        @Param("movieId") Long movieId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        @Query("SELECT s FROM Screening s WHERE s.startTime BETWEEN :startTime AND :endTime ORDER BY s.startTime")
        List<Screening> findByStartTimeBetweenOrderByStartTime(
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Query("SELECT s FROM Screening s WHERE s.isHighlighted = true AND s.startTime BETWEEN :startTime AND :endTime ORDER BY s.startTime")
        List<Screening> findByIsHighlightedTrueAndStartTimeBetween(
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.startTime > :startTime AND s.endTime < :endTime ORDER BY s.startTime")
        List<Screening> findByMovieIdAndStartTimeAfterAndEndTimeBefore(
                        @Param("movieId") Long movieId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.startTime BETWEEN :startTime AND :endTime ORDER BY s.startTime")
        List<Screening> findByMovieIdAndStartTimeBetween(
                        @Param("movieId") Long movieId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}