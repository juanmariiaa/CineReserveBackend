package org.example.backend.repository;

import org.example.backend.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {


    @Query("SELECT s FROM Screening s WHERE s.room.id = :roomId AND " +
            "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Screening> findOverlappingScreenings(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);


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
}