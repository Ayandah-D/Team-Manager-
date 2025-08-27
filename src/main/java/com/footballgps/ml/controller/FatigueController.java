package com.footballgps.ml.controller;

import com.footballgps.ml.model.MLPrediction;
import com.footballgps.ml.service.FatigueDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FatigueController {
    
    private final FatigueDetectionService fatigueDetectionService;
    
    @PostMapping("/fatigue-detection/{playerId}/session/{sessionId}")
    public ResponseEntity<MLPrediction> detectFatigue(
            @PathVariable String playerId,
            @PathVariable String sessionId) {
        log.info("Detecting fatigue for player: {} in session: {}", playerId, sessionId);
        MLPrediction prediction = fatigueDetectionService.detectFatigue(playerId, sessionId);
        return ResponseEntity.ok(prediction);
    }
}
