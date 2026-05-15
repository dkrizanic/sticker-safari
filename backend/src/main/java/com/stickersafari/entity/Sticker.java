package com.stickersafari.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sticker")
public class Sticker {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sticker_id_seq")
    @SequenceGenerator(name = "sticker_id_seq", sequenceName = "sticker_id_seq", allocationSize = 1)
    private Long id;

    private String name;
    private String latinName;
    private String description;
    private String habitat;
    private String src;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name= "album")
    private Album album;

}
