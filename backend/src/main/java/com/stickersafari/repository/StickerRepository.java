package com.stickersafari.repository;

import com.stickersafari.entity.Sticker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {

    List<Sticker> findByAlbumId(Long albumId);
    
    @Query("SELECT s FROM Sticker s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.latinName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.habitat) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Sticker> searchStickers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT s FROM Sticker s WHERE s.album.id = :albumId")
    Page<Sticker> findByAlbumIdWithPagination(@Param("albumId") Long albumId, Pageable pageable);
}
