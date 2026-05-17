package com.stickersafari.config;

import com.stickersafari.entity.Collector;
import com.stickersafari.repository.CollectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CollectorRepository collectorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check if it's the admin user
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .roles("ADMIN")
                    .build();
        }

        // Load collector from database
        Collector collector = collectorRepository.findByUsername(username);
        if (collector == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // For now, all database users have the default password "collector"
        // In a real system, the password would be stored in the database
        return User.builder()
                .username(collector.getUsername())
                .password(passwordEncoder.encode("collector"))
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_COLLECTOR")))
                .build();
    }
}
