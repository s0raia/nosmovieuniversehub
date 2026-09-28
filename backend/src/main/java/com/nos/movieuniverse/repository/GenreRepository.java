package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
