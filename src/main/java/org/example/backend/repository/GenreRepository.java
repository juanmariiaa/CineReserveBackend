package org.example.backend.repository;

import org.example.backend.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId);
}