package com.stickersafari.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;

@Entity
@Data
@Table(name = "sticker")
public class Sticker {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sticker_id_seq")
    @SequenceGenerator(name = "sticker_id_seq", sequenceName = "sticker_id_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Sticker name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;
    
    @Size(max = 255)
    private String latinName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Size(max = 255)
    private String habitat;
    
    @Size(max = 500)
    private String src;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "album")
    @ToString.Exclude  // Prevent circular reference with Album
    private Album album;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sticker)) return false;
        Sticker sticker = (Sticker) o;
        return id != null && Objects.equals(id, sticker.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
