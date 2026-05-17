package com.stickersafari.controller;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.mapper.CollectorMapper;
import com.stickersafari.service.CollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collectors")
@RequiredArgsConstructor
@Slf4j
public class CollectorController {

    private final CollectorService collectorService;
    private final CollectorMapper collectorMapper;

    @GetMapping
    public ResponseEntity<List<CollectorResponse>> getAllCollectors() {
        log.debug("Getting all collectors");
        List<Collector> collectors = collectorService.getAllCollectors();
        return ResponseEntity.ok(collectorMapper.toResponseList(collectors));
    }

    @GetMapping("/{id}")
    public CollectorResponse getCollector(@PathVariable("id") Long id) {
        log.debug("Getting collector with ID: {}", id);
        Collector collector = collectorService.findById(id);
        return collectorMapper.toResponse(collector);
    }

    @GetMapping("/{id}/stickers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<Long>> getCollectorOwnedStickers(@PathVariable("id") Long id) {
        log.debug("Getting owned stickers for collector with ID: {}", id);
        Collector collector = collectorService.findById(id);
        List<Long> ownedStickerIds = collector.getOwnedStickers().keySet().stream()
                .collect(Collectors.toList());
        return ResponseEntity.ok(ownedStickerIds);
    }

    @GetMapping("/username/{username}")
    public CollectorResponse getCollectorByUsername(@PathVariable("username") String username) {
        log.debug("Getting collector with username: {}", username);
        Collector collector = collectorService.findByUsername(username);
        if (collector == null) {
            throw new RuntimeException("Collector not found with username: " + username);
        }
        return collectorMapper.toResponse(collector);
    }

    @PostMapping
    public ResponseEntity<CollectorResponse> createCollector(@RequestBody CollectorRequest request) {
        log.info("Creating collector: {}", request.getUsername());
        Collector collector = collectorService.createCollector(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(collectorMapper.toResponse(collector));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectorResponse> updateCollector(@PathVariable("id") Long id,
                                                              @RequestBody CollectorRequest request,
                                                              Authentication authentication) {
        log.info("Updating collector with ID: {}", id);
        
        // Check authorization: user can only update their own profile unless they are admin
        if (!isAuthorizedToModify(id, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own profile");
        }
        
        Collector collector = collectorService.updateCollector(id, request);
        return ResponseEntity.ok(collectorMapper.toResponse(collector));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollector(@PathVariable("id") Long id, Authentication authentication) {
        log.info("Deleting collector with ID: {}", id);
        
        // Check authorization: user can only delete their own profile unless they are admin
        if (!isAuthorizedToModify(id, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own profile");
        }
        
        collectorService.deleteCollector(id);
        return ResponseEntity.noContent().build();
    }
    
    private boolean isAuthorizedToModify(Long collectorId, Authentication authentication) {
        // Admins can modify any collector
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }
        
        // Collectors can only modify their own profile
        String username = authentication.getName();
        Collector collector = collectorService.findById(collectorId);
        return collector.getUsername().equals(username);
    }
}
