package com.lifeplus.healthcare.config;

import com.lifeplus.healthcare.modules.auth.JwtAuthFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                // Stateless — no HTTP session
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI docs — always public
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Auth endpoints — public
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Actuator health — public
                        .requestMatchers("/actuator/health").permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/approvals/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/notifications/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/slides/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/slides/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/slides/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/slides/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/walkthrough/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/walkthrough/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/walkthrough/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/walkthrough/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/settings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/settings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/settings/**").hasRole("ADMIN")
                        // Public read-only: settings, slides, walkthrough (Android reads these)
                        .requestMatchers(HttpMethod.GET, "/api/v1/settings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/slides/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/walkthrough/**").permitAll()
                        // Public read-only endpoints (browse hospitals, doctors, etc.)
                        .requestMatchers(HttpMethod.GET, "/api/v1/donors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/hospitals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/clinics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ambulances/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/pharmacies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/diagnostics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/blood-banks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/blood-organizations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/blood-orgs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/blood-orgs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/blood-inventory/**").permitAll()
                        // Guest-allowed POST endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/blood-requests").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/emergency-requests").permitAll()
                        // Public GET for blood/emergency requests
                        .requestMatchers(HttpMethod.GET, "/api/v1/blood-requests/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/emergency-requests/**").permitAll()
                        // Public read-only reviews for entity detail pages
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                        // Premium check — public GET (returns allowed/denied info)
                        .requestMatchers(HttpMethod.GET, "/api/v1/premium/check").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** BCryptPasswordEncoder bean — used by AuthController for password hashing */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
