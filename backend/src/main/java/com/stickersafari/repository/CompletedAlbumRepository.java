package com.stickersafari.repository;

import com.stickersafari.entity.CompletedAlbum;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompletedAlbumRepository extends JpaRepository<CompletedAlbum, Long> {

    @Query("""
        SELECT c.collector.id, c.collector.username, COUNT(c) as completions
        FROM CompletedAlbum c
        WHERE c.completedAt >= :startDate AND c.completedAt < :endDate
        GROUP BY c.collector.id, c.collector.username
        ORDER BY completions DESC
        """)
    List<Object[]> findTopCollectorsByPeriod(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    @Query("""
        SELECT c.id, c.username, c.stars
        FROM Collector c
        ORDER BY c.stars DESC
        """)
    List<Object[]> findTopCollectorsAllTime(Pageable pageable);
}
