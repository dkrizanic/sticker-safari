package com.stickersafari.controller;

import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.mapper.CollectorMapper;
import com.stickersafari.service.CollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collectors")
@RequiredArgsConstructor
@Slf4j
public class CollectorController {

    private final CollectorService collectorService;
    private final CollectorMapper collectorMapper;

    @GetMapping("/{id}")
    public CollectorResponse getCollector(@PathVariable("id") Long id) {
        log.debug("Getting collector with ID: {}", id);
        Collector collector = collectorService.findById(id);
        return collectorMapper.toResponse(collector);
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
}
