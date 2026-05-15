package com.stickersafari.repository;

import com.stickersafari.entity.CollectorSticker;
import com.stickersafari.entity.CollectorStickerKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectorStickerRepository extends JpaRepository<CollectorSticker, CollectorStickerKey> {
}
