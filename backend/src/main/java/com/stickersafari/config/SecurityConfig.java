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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.stickersafari.entity.Role;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Create in-memory users for collectors (from seed data)
        UserDetails bob = User.builder()
                .username("Bob")
                .password(passwordEncoder.encode("collector"))
                .roles(Role.COLLECTOR.name())
                .build();

        UserDetails jack = User.builder()
                .username("Jack")
                .password(passwordEncoder.encode("collector"))
                .roles(Role.COLLECTOR.name())
                .build();

        UserDetails anne = User.builder()
                .username("Anne")
                .password(passwordEncoder.encode("collector"))
                .roles(Role.COLLECTOR.name())
                .build();

        UserDetails ema = User.builder()
                .username("Ema")
                .password(passwordEncoder.encode("collector"))
                .roles(Role.COLLECTOR.name())
                .build();

        // Create admin users
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles(Role.ADMIN.name())
                .build();

        return new InMemoryUserDetailsManager(bob, jack, anne, ema, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Disable CSRF for development
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Must be FIRST (more specific patterns)
                        .requestMatchers(HttpMethod.POST, "/collectors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/collectors/username/**").permitAll()
                        
                        // COLLECTOR role - Read-only access and collection activities
                        .requestMatchers(HttpMethod.GET, "/album/**").hasAnyRole(Role.COLLECTOR.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/stickers/**").hasAnyRole(Role.COLLECTOR.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/collectors/**").hasAnyRole(Role.COLLECTOR.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/rankings/**").hasRole(Role.COLLECTOR.name())
                        .requestMatchers(HttpMethod.GET, "/notifications/**").hasRole(Role.COLLECTOR.name())
                        .requestMatchers(HttpMethod.POST, "/stickers/open/**").hasRole(Role.COLLECTOR.name())
                        .requestMatchers(HttpMethod.POST, "/album/*/complete/**").hasRole(Role.COLLECTOR.name())
                        .requestMatchers(HttpMethod.POST, "/tradeoffer/**").hasRole(Role.COLLECTOR.name())
                        .requestMatchers(HttpMethod.GET, "/tradeoffer/**").hasRole(Role.COLLECTOR.name())
                        
                        // ADMIN role - CRUD access but no collection activities
                        .requestMatchers(HttpMethod.POST, "/album").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/album/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/album/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/stickers").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/stickers/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/stickers/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/collectors/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/collectors/**").hasRole(Role.ADMIN.name())
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}); // Enable HTTP Basic authentication

        return http.build();
    }
}
