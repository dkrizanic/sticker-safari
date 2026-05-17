package com.stickersafari.repository;

import com.stickersafari.entity.CompletedAlbum;
import org.springframework.data.domain.Page;
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
        SELECT COUNT(DISTINCT c.collector.id)
        FROM CompletedAlbum c
        WHERE c.album.id = :albumId
        """)
    long countDistinctCollectorsByAlbum(@Param("albumId") Long albumId);

    // Legacy method - returns all results
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

    // New method with search support
    @Query("""
        SELECT c.collector.id, c.collector.username, COUNT(c) as completions
        FROM CompletedAlbum c
        WHERE c.completedAt >= :startDate AND c.completedAt < :endDate
        AND (:search IS NULL OR LOWER(CAST(c.collector.username AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        GROUP BY c.collector.id, c.collector.username
        ORDER BY completions DESC
        """)
    List<Object[]> findTopCollectorsByPeriodWithSearch(@Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate,
                                                         @Param("search") String search,
                                                         Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT c.collector.id)
        FROM CompletedAlbum c
        WHERE c.completedAt >= :startDate AND c.completedAt < :endDate
        AND (:search IS NULL OR LOWER(CAST(c.collector.username AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        """)
    long countCollectorsByPeriodWithSearch(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("search") String search);

    // Legacy method - returns all results
    @Query("""
        SELECT c.id, c.username, c.stars, COUNT(ca.id) as completions, (c.stars * 5 + COUNT(ca.id)) as score
        FROM Collector c
        LEFT JOIN CompletedAlbum ca ON ca.collector.id = c.id
        GROUP BY c.id, c.username, c.stars
        HAVING (c.stars * 5 + COUNT(ca.id)) > 0
        ORDER BY score DESC
        """)
    List<Object[]> findTopCollectorsAllTime(Pageable pageable);

    // New method with search support
    @Query("""
        SELECT c.id, c.username, c.stars, COUNT(ca.id) as completions, (COALESCE(c.stars, 0) * 5 + COUNT(ca.id)) as score
        FROM Collector c
        LEFT JOIN CompletedAlbum ca ON ca.collector.id = c.id
        WHERE :search IS NULL OR LOWER(CAST(c.username AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
        GROUP BY c.id, c.username, c.stars
        HAVING (COALESCE(c.stars, 0) * 5 + COUNT(ca.id)) > 0
        ORDER BY score DESC
        """)
    List<Object[]> findTopCollectorsAllTimeWithSearch(@Param("search") String search, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT c.id)
        FROM Collector c
        WHERE (:search IS NULL OR LOWER(CAST(c.username AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (COALESCE(c.stars, 0) > 0 OR EXISTS (SELECT 1 FROM CompletedAlbum ca WHERE ca.collector.id = c.id))
        """)
    long countCollectorsAllTimeWithSearch(@Param("search") String search);
}
