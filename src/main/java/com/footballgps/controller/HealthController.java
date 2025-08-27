package com.footballgps.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class HealthController {
    
    private final MongoTemplate mongoTemplate;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Football GPS Tracking System");
        
        try {
            // Test MongoDB connection
            mongoTemplate.getCollection("players").estimatedDocumentCount();
            health.put("database", "Connected");
        } catch (Exception e) {
            log.error("Database connection failed", e);
            health.put("database", "Disconnected");
            health.put("error", e.getMessage());
        }
        
        log.info("Health check completed: {}", health);
        return ResponseEntity.ok(health);
    }
}
