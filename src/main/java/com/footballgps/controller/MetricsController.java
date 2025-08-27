package com.footballgps.controller;

import com.footballgps.model.PlayerMetrics;
import com.footballgps.service.MetricsCalculationService;
import com.footballgps.service.PlayerMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MetricsController {
    
    private final PlayerMetricsService playerMetricsService;
    private final MetricsCalculationService metricsCalculationService;
    
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<PlayerMetrics>> getPlayerMetrics(@PathVariable String playerId) {
        List<PlayerMetrics> metrics = playerMetricsService.getPlayerMetrics(playerId);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/player/{playerId}/session/{sessionId}")
    public ResponseEntity<PlayerMetrics> getPlayerSessionMetrics(
            @PathVariable String playerId,
            @PathVariable String sessionId) {
        return playerMetricsService.getPlayerSessionMetrics(playerId, sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<PlayerMetrics>> getSessionMetrics(@PathVariable String sessionId) {
        List<PlayerMetrics> metrics = playerMetricsService.getSessionMetrics(sessionId);
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping("/calculate/player/{playerId}/session/{sessionId}")
    public ResponseEntity<PlayerMetrics> calculateSessionMetrics(
            @PathVariable String playerId,
            @PathVariable String sessionId) {
        PlayerMetrics metrics = metricsCalculationService.calculateSessionMetrics(playerId, sessionId);
        if (metrics != null) {
            PlayerMetrics saved = playerMetricsService.saveMetrics(metrics);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/player/{playerId}/range")
    public ResponseEntity<List<PlayerMetrics>> getPlayerMetricsInRange(
            @PathVariable String playerId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        List<PlayerMetrics> metrics = playerMetricsService.getPlayerMetricsInRange(playerId, startTime, endTime);
        return ResponseEntity.ok(metrics);
    }
}
