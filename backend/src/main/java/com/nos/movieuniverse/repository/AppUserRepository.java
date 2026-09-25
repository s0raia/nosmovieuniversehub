package com.nos.movieuniverse.repository;

import com.nos.movieuniverse.domain.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** Username is the seed's upsert key, so this drives the idempotent import. */
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
