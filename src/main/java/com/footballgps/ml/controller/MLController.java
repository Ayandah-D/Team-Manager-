package com.footballgps.ml.controller;

import com.footballgps.ml.model.MLPrediction;
import com.footballgps.ml.service.InjuryPredictionService;
import com.footballgps.ml.service.PerformanceOptimizationService;
import com.footballgps.ml.service.TacticalAnalysisService;
import com.footballgps.ml.service.MLPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class MLController {
    
    private final InjuryPredictionService injuryPredictionService;
    private final PerformanceOptimizationService performanceOptimizationService;
    private final TacticalAnalysisService tacticalAnalysisService;
    private final MLPredictionService mlPredictionService;
    
    @PostMapping("/injury-risk/{playerId}")
    public ResponseEntity<MLPrediction> predictInjuryRisk(@PathVariable String playerId) {
        log.info("Predicting injury risk for player: {}", playerId);
        MLPrediction prediction = injuryPredictionService.predictInjuryRisk(playerId);
        return ResponseEntity.ok(prediction);
    }
    
    @PostMapping("/optimize-performance/{playerId}")
    public ResponseEntity<MLPrediction> optimizePerformance(@PathVariable String playerId) {
        log.info("Optimizing performance for player: {}", playerId);
        MLPrediction prediction = performanceOptimizationService.optimizePerformance(playerId);
        return ResponseEntity.ok(prediction);
    }
    
    @PostMapping("/tactical-analysis/{sessionId}")
    public ResponseEntity<MLPrediction> analyzeTacticalPerformance(@PathVariable String sessionId) {
        log.info("Analyzing tactical performance for session: {}", sessionId);
        MLPrediction prediction = tacticalAnalysisService.analyzeTacticalPerformance(sessionId);
        return ResponseEntity.ok(prediction);
    }
    
    @PostMapping("/optimal-position/{playerId}/session/{sessionId}")
    public ResponseEntity<MLPrediction> predictOptimalPosition(
            @PathVariable String playerId, 
            @PathVariable String sessionId) {
        log.info("Predicting optimal position for player: {} in session: {}", playerId, sessionId);
        MLPrediction prediction = tacticalAnalysisService.predictOptimalPosition(playerId, sessionId);
        return ResponseEntity.ok(prediction);
    }
    
    @GetMapping("/predictions/player/{playerId}")
    public ResponseEntity<List<MLPrediction>> getPlayerPredictions(@PathVariable String playerId) {
        List<MLPrediction> predictions = mlPredictionService.getPlayerPredictions(playerId);
        return ResponseEntity.ok(predictions);
    }
    
    @GetMapping("/predictions/session/{sessionId}")
    public ResponseEntity<List<MLPrediction>> getSessionPredictions(@PathVariable String sessionId) {
        List<MLPrediction> predictions = mlPredictionService.getSessionPredictions(sessionId);
        return ResponseEntity.ok(predictions);
    }
    
    @GetMapping("/predictions/type/{type}")
    public ResponseEntity<List<MLPrediction>> getPredictionsByType(@PathVariable MLPrediction.PredictionType type) {
        List<MLPrediction> predictions = mlPredictionService.getPredictionsByType(type);
        return ResponseEntity.ok(predictions);
    }
    
    @GetMapping("/predictions/recent")
    public ResponseEntity<List<MLPrediction>> getRecentPredictions(
            @RequestParam(defaultValue = "24") int hours) {
        List<MLPrediction> predictions = mlPredictionService.getRecentPredictions(hours);
        return ResponseEntity.ok(predictions);
    }
}
