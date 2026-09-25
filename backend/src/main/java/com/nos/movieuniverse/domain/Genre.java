package com.nos.movieuniverse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** A TMDB genre. The id is TMDB's, so it is never generated locally. */
@Entity
public class Genre {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Genre() {
        // required by JPA
    }

    public Genre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
