package com.nos.movieuniverse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security beans.
 *
 * <p>The catalogue is public because it is: film data from TMDB, and playlists
 * the seed already publishes. Everything else stays locked behind the defaults.
 * Login, sessions and the rules guarding writes arrive with the auth work, at
 * which point the blanket permit on {@code GET /api/**} narrows to the
 * endpoints that really are anonymous.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .permitAll()
                        .requestMatchers("/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .httpBasic(basic -> {})
                // Safe while the API is read-only. This has to come back, scoped to
                // the write endpoints, as soon as anything accepts a POST.
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
