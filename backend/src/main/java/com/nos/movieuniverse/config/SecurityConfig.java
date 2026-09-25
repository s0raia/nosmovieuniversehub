package com.nos.movieuniverse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security beans.
 *
 * <p>No {@code SecurityFilterChain} yet, so Spring Security's defaults still
 * apply: every endpoint is locked and a password is printed at startup. That is
 * intentional for now - form login and the session setup arrive with the auth
 * work. The encoder is here already because the seed importer needs to hash the
 * passwords it invents for the seeded users.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
