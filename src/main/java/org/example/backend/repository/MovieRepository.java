package org.example.backend.repository;

import org.example.backend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTmdbId(Integer tmdbId);

    Optional<Movie> findByTmdbId(Integer tmdbId);

    List<Movie> findByIsActiveTrue();

    List<Movie> findByIsFeaturedTrue();

}