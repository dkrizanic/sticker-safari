package com.stickersafari.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

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

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
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

    public void addSticker(Long stickerId) {
        if (this.ownedStickers.containsKey(stickerId)){
            this.ownedStickers.put(stickerId, this.ownedStickers.get(stickerId) + 1);
        } else {
            this.ownedStickers.put(stickerId, 1);
        }
    }

    public void removeSticker(Long stickerId) throws Exception {
        if (this.ownedStickers.containsKey(stickerId)){
            Integer newValue = this.ownedStickers.get(stickerId) - 1;
            if(newValue > 0) {
                this.ownedStickers.put(stickerId, newValue);
            }
        } else {
            throw new Exception("Can't remove sticker " + stickerId + " because " + this.getUsername() + " does not have it");
        }
    }
    
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

    @Override
    public String toString() {
        return "Collector{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
