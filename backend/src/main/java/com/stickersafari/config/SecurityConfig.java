package com.stickersafari.config;

import com.stickersafari.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.stickersafari.enums.Role;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles(Role.ADMIN.name())
                .build();

        UserDetails collector1 = User.builder()
                .username("collector1")
                .password(passwordEncoder().encode("pass123"))
                .roles(Role.COLLECTOR.name())
                .build();

        UserDetails collector2 = User.builder()
                .username("collector2")
                .password(passwordEncoder().encode("pass123"))
                .roles(Role.COLLECTOR.name())
                .build();

        return new InMemoryUserDetailsManager(admin, collector1, collector2);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Authentication
                        .requestMatchers("/auth/**").permitAll()
                        // Public endpoints - Registration and user lookup
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
                        .requestMatchers(HttpMethod.PUT, "/collectors/**").hasAnyRole(Role.ADMIN.name(), Role.COLLECTOR.name())
                        .requestMatchers(HttpMethod.DELETE, "/collectors/**").hasAnyRole(Role.ADMIN.name(), Role.COLLECTOR.name())
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
