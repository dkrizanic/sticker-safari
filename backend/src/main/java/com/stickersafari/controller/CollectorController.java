package com.stickersafari.controller;

import com.stickersafari.dto.CollectorResponse;
import com.stickersafari.entity.Collector;
import com.stickersafari.service.CollectorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collectors")
public class CollectorController {

    private CollectorService collectorService;

    public CollectorController(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @GetMapping("/{id}")
    public CollectorResponse getCollector(@PathVariable("id") Long id) {
        Collector collector = collectorService.findById(id);
        return mapCollectorToCollectorResponse(collector);
    }

    @GetMapping("/username/{username}")
    public CollectorResponse getCollectorByUsername(@PathVariable("username") String username) {
        Collector collector = collectorService.findByUsername(username);
        if (collector == null) {
            throw new RuntimeException("Collector not found with username: " + username);
        }
        return mapCollectorToCollectorResponse(collector);
    }

    private CollectorResponse mapCollectorToCollectorResponse(Collector collector) {
        CollectorResponse response = new CollectorResponse();
        response.setId(collector.getId());
        response.setUsername(collector.getUsername());
        return response;
    }

}
