package com.stickersafari.repository;

import com.stickersafari.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {
    Collector findByUsername(String username);
}