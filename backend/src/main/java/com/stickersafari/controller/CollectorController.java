package com.stickersafari.controller;

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
    public Collector getCollector(@PathVariable("id") Long id) {
        return collectorService.findById(id);
    }

}
