package com.stickersafari.entity;

import jakarta.persistence.*;
import javax.persistence.Entity;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Table(name = "collector")
public class Collector {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collector_id_seq")
    @SequenceGenerator(name = "collector_id_seq", sequenceName = "collector_id_seq", allocationSize = 1)
    private Long id;

    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collector_sticker", joinColumns = @JoinColumn(name = "collector_id"))
    @MapKeyColumn(name = "sticker_id")
    @Column(name = "quantity")
    private Map<Long, Integer> ownedStickers = new HashMap<>();

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

}
