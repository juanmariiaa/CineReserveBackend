package org.example.backend.repository;

import org.example.backend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Find all movies where isActive is true
     *
     * @return List of active movies
     */
    List<Movie> findByIsActiveTrue();

    /**
     * Optional: Additional useful methods you might want
     */
    List<Movie> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title);

    List<Movie> findTop10ByIsActiveTrueOrderByReleaseDateDesc();

    List<Movie> findByTmdbId(Integer tmdbId);
}