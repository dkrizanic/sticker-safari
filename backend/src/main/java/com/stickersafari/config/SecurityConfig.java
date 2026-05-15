package com.stickersafari.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Create in-memory users for collectors (from seed data)
        UserDetails bob = User.builder()
                .username("Bob")
                .password(passwordEncoder.encode("collector"))
                .roles("COLLECTOR")
                .build();

        UserDetails jack = User.builder()
                .username("Jack")
                .password(passwordEncoder.encode("collector"))
                .roles("COLLECTOR")
                .build();

        UserDetails anne = User.builder()
                .username("Anne")
                .password(passwordEncoder.encode("collector"))
                .roles("COLLECTOR")
                .build();

        UserDetails ema = User.builder()
                .username("Ema")
                .password(passwordEncoder.encode("collector"))
                .roles("COLLECTOR")
                .build();

        // Create admin users
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(bob, jack, anne, ema, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for development
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - POST /collectors (user registration)
                        .requestMatchers(HttpMethod.POST, "/collectors").permitAll()
                        
                        // COLLECTOR role - Read-only access
                        .requestMatchers(HttpMethod.GET, "/albums/**").hasRole("COLLECTOR")
                        .requestMatchers(HttpMethod.GET, "/stickers/**").hasRole("COLLECTOR")
                        .requestMatchers(HttpMethod.GET, "/collectors/**").hasRole("COLLECTOR")
                        .requestMatchers(HttpMethod.POST, "/stickers/open-pack").hasRole("COLLECTOR")
                        .requestMatchers(HttpMethod.POST, "/albums/*/complete/**").hasRole("COLLECTOR")
                        .requestMatchers(HttpMethod.GET, "/rankings/**").hasRole("COLLECTOR")
                        
                        // ADMIN role - CRUD access but no pack opening
                        .requestMatchers(HttpMethod.POST, "/albums").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/albums/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/albums/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/stickers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/stickers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/stickers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/collectors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/collectors/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}); // Enable HTTP Basic authentication

        return http.build();
    }
}
