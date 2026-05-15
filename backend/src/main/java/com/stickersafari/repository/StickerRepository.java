package com.stickersafari.repository;

import com.stickersafari.entity.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {

    @Query("SELECT s FROM Sticker s WHERE s.album.id = ?1")
    List<Sticker> getStickersOfAlbum(Long albumId);
}
