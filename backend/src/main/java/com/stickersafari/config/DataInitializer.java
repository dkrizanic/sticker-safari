package com.stickersafari.config;

import com.stickersafari.entity.Album;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.CollectorSticker;
import com.stickersafari.entity.CollectorStickerKey;
import com.stickersafari.entity.Sticker;
import com.stickersafari.repository.AlbumRepository;
import com.stickersafari.repository.CollectorRepository;
import com.stickersafari.repository.CollectorStickerRepository;
import com.stickersafari.repository.StickerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            CollectorRepository collectorRepository,
            AlbumRepository albumRepository,
            StickerRepository stickerRepository,
            CollectorStickerRepository collectorStickerRepository) {
        
        return args -> {
            // Only initialize if database is empty
            if (collectorRepository.count() > 0) {
                return;
            }

            // Create collectors
            Collector bob = new Collector();
            bob.setUsername("Bob");
            
            Collector jack = new Collector();
            jack.setUsername("Jack");
            
            Collector anne = new Collector();
            anne.setUsername("Anne");
            
            Collector ema = new Collector();
            ema.setUsername("Ema");
            
            collectorRepository.saveAll(List.of(bob, jack, anne, ema));

            // Create albums
            Album africanSafari = new Album();
            africanSafari.setName("African Safari");
            
            Album oceanLife = new Album();
            oceanLife.setName("Ocean Life");
            
            albumRepository.saveAll(List.of(africanSafari, oceanLife));

            // Create stickers for African Safari
            Sticker lion = createSticker("Lion", "Panthera leo", 
                "The lion is a large carnivorous feline mammal.", "Savanna", 
                africanSafari, "https://images.unsplash.com/photo-1546182990-dffeafbe841d?q=80&w=2059&auto=format&fit=crop");
            
            Sticker elephant = createSticker("Elephant", "Loxodonta africana",
                "The elephant is the largest land mammal.", "Grasslands and forests",
                africanSafari, "https://images.unsplash.com/photo-1581852017103-68ac65514cf7?q=80&w=2073&auto=format&fit=crop");
            
            Sticker giraffe = createSticker("Giraffe", "Giraffa camelopardalis",
                "The giraffe is known for its long neck and distinctive spotted pattern.", "Open woodlands and grasslands",
                africanSafari, "https://plus.unsplash.com/premium_photo-1661813434310-98cca4c9135e?q=80&w=2067&auto=format&fit=crop");
            
            Sticker zebra = createSticker("Zebra", "Equus zebra",
                "The zebra is known for its black and white striped coat.", "Grasslands",
                africanSafari, "https://images.unsplash.com/photo-1526319238109-524eecb9b913?q=80&w=2024&auto=format&fit=crop");
            
            Sticker tiger = createSticker("Tiger", "Panthera tigris",
                "The tiger is the largest cat species.", "Various habitats including forests and grasslands",
                africanSafari, "https://images.unsplash.com/photo-1549480017-d76466a4b7e8?q=80&w=2056&auto=format&fit=crop");
            
            Sticker penguin = createSticker("Penguin", "Spheniscidae",
                "Penguins are flightless birds adapted to life in the water.", "Antarctic and subantarctic regions",
                africanSafari, "https://images.unsplash.com/photo-1551415923-a2297c7fda79?q=80&w=1932&auto=format&fit=crop");
            
            Sticker kangaroo = createSticker("Kangaroo", "Macropodidae",
                "Kangaroos are marsupials known for their powerful hind legs.", "Grasslands and deserts",
                africanSafari, "https://images.unsplash.com/photo-1575699914911-0027c7b95fb6?q=80&w=2071&auto=format&fit=crop");

            // Create stickers for Ocean Life
            Sticker dolphin = createSticker("Dolphin", "Delphinus delphis",
                "Dolphins are highly intelligent marine mammals.", "Oceans and seas",
                oceanLife, "https://images.unsplash.com/photo-1607153333879-c174d265f1d2?q=80&w=2071&auto=format&fit=crop");
            
            Sticker whale = createSticker("Whale", "Balaenoptera musculus",
                "The blue whale is the largest animal on Earth.", "Deep oceans",
                oceanLife, "https://images.unsplash.com/photo-1568430462989-44163eb1752f?q=80&w=2074&auto=format&fit=crop");
            
            Sticker shark = createSticker("Shark", "Carcharodon carcharias",
                "The great white shark is an apex predator.", "Coastal waters",
                oceanLife, "https://images.unsplash.com/photo-1560275619-4662e36fa65c?q=80&w=2069&auto=format&fit=crop");

            stickerRepository.saveAll(List.of(lion, elephant, giraffe, zebra, tiger, penguin, kangaroo, dolphin, whale, shark));

            // Give Bob some initial stickers
            CollectorSticker bobLion = createCollectorSticker(bob, lion, 2);
            CollectorSticker bobElephant = createCollectorSticker(bob, elephant, 1);
            CollectorSticker bobTiger = createCollectorSticker(bob, tiger, 3);
            
            collectorStickerRepository.saveAll(List.of(bobLion, bobElephant, bobTiger));
            
            System.out.println("✅ Database initialized with test data");
        };
    }

    private Sticker createSticker(String name, String latinName, String description, 
                                   String habitat, Album album, String src) {
        Sticker sticker = new Sticker();
        sticker.setName(name);
        sticker.setLatinName(latinName);
        sticker.setDescription(description);
        sticker.setHabitat(habitat);
        sticker.setAlbum(album);
        sticker.setSrc(src);
        return sticker;
    }

    private CollectorSticker createCollectorSticker(Collector collector, Sticker sticker, int quantity) {
        CollectorSticker cs = new CollectorSticker();
        
        CollectorStickerKey key = new CollectorStickerKey();
        key.setCollectorId(collector.getId());
        key.setStickerId(sticker.getId());
        
        cs.setId(key);
        cs.setCollector(collector);
        cs.setSticker(sticker);
        cs.setQuantity(quantity);
        
        return cs;
    }
}
