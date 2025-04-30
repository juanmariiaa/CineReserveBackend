package org.example.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "genre")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "tmdb_genre_id")
    private Integer tmdbGenreId;

    @JsonBackReference
    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies = new HashSet<>();

    // Constructor con parámetros (sin incluir movies)
    public Genre(Long id, String name, Integer tmdbGenreId) {
        this.id = id;
        this.name = name;
        this.tmdbGenreId = tmdbGenreId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return Objects.equals(id, genre.id) &&
                Objects.equals(name, genre.name) &&
                Objects.equals(tmdbGenreId, genre.tmdbGenreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, tmdbGenreId);
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tmdbGenreId=" + tmdbGenreId +
                '}';
    }
}