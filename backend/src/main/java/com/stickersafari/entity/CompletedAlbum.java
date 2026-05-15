package com.stickersafari.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "completed_album")
public class CompletedAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "completed_album_id_seq")
    @SequenceGenerator(name = "completed_album_id_seq", sequenceName = "completed_album_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collector_id", nullable = false)
    private Collector collector;

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
}
