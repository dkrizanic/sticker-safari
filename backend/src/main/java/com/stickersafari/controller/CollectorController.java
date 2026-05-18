package com.stickersafari.controller;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.mapper.CollectorMapper;
import com.stickersafari.service.CollectorService;
import com.stickersafari.util.PaginationResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<List<Long>> getCollectorOwnedStickers(@PathVariable("id") Long id) {
        log.debug("Getting owned stickers for collector with ID: {}", id);
        List<Long> ownedStickerIds = collectorService.getOwnedStickerIds(id);
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
        
        // Check authorization
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!collectorService.canModifyCollector(id, authentication.getName(), isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own profile");
        }
        
        Collector collector = collectorService.updateCollector(id, request);
        return ResponseEntity.ok(collectorMapper.toResponse(collector));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollector(@PathVariable("id") Long id, Authentication authentication) {
        log.info("Deleting collector with ID: {}", id);
        
        // Check authorization
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!collectorService.canModifyCollector(id, authentication.getName(), isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own profile");
        }
        
        collectorService.deleteCollector(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/paginated")
    public ResponseEntity<Map<String, Object>> getCollectorsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        
        log.debug("Admin: Getting paginated collectors - page: {}, size: {}, sortBy: {}, sortDir: {}, search: {}", 
                  page, size, sortBy, sortDir, search);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Collector> collectorPage;
        if (search != null && !search.trim().isEmpty()) {
            collectorPage = collectorService.searchCollectors(search, pageable);
        } else {
            collectorPage = collectorService.getAllCollectorsWithPagination(pageable);
        }
        
        List<CollectorResponse> collectors = collectorPage.getContent().stream()
                .map(collectorMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(PaginationResponseBuilder.build(collectors, collectorPage, "collectors"));
    }
}
