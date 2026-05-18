package com.stickersafari.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Data
@Table(name = "collector")
public class Collector {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collector_id_seq")
    @SequenceGenerator(name = "collector_id_seq", sequenceName = "collector_id_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude  // Prevent circular reference with User
    private User user;

    @Column(name = "username")
    private String username;

    @ElementCollection
    @CollectionTable(name = "collector_sticker", joinColumns = @JoinColumn(name = "collector_id"))
    @MapKeyColumn(name = "sticker_id")
    @Column(name = "quantity")
    private Map<Long, Integer> ownedStickers = new HashMap<>();

    @Column(name = "last_pack_open_date")
    private LocalDate lastPackOpenDate;

    @Column(name = "daily_sticker_count")
    private Integer dailyStickerCount = 0;

    @Column(name = "stars")
    private Integer stars = 0;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Collector)) return false;
        Collector collector = (Collector) o;
        return id != null && Objects.equals(id, collector.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
