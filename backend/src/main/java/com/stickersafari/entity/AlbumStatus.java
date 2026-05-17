package com.stickersafari.entity;

public enum AlbumStatus {
    PENDING,    // Album created but not published (may not have stickers yet)
    PUBLISHED   // Album published and visible to collectors (must have at least one sticker)
}
