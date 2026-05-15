package com.stickersafari.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "album")
@Data
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_id_seq")
    @SequenceGenerator(name = "album_id_seq", sequenceName = "album_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL)
    private List<Sticker> stickers;

}
