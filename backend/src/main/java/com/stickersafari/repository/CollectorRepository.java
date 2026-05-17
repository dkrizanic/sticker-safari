package com.stickersafari.repository;

import com.stickersafari.entity.Collector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {
    Collector findByUsername(String username);
    
    @Query("SELECT c FROM Collector c WHERE LOWER(c.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Collector> searchCollectors(@Param("search") String search, Pageable pageable);
}