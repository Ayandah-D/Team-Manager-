package com.footballgps.controller;

import com.footballgps.model.GpsData;
import com.footballgps.service.GpsDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GpsDataController {
    
    private final GpsDataService gpsDataService;
    
    @PostMapping("/data")
    public ResponseEntity<GpsData> receiveGpsData(@RequestBody GpsData gpsData) {
        GpsData saved = gpsDataService.saveGpsData(gpsData);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<GpsData>> getSessionData(@PathVariable String sessionId) {
        List<GpsData> data = gpsDataService.getSessionData(sessionId);
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/player/{playerId}/session/{sessionId}")
    public ResponseEntity<List<GpsData>> getPlayerSessionData(
            @PathVariable String playerId, 
            @PathVariable String sessionId) {
        List<GpsData> data = gpsDataService.getPlayerSessionData(playerId, sessionId);
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/session/{sessionId}/recent")
    public ResponseEntity<List<GpsData>> getRecentData(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "5") int minutes) {
        List<GpsData> data = gpsDataService.getRecentData(sessionId, minutes);
        return ResponseEntity.ok(data);
    }
    
    @GetMapping("/player/{playerId}/range")
    public ResponseEntity<List<GpsData>> getPlayerDataInRange(
            @PathVariable String playerId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        List<GpsData> data = gpsDataService.getPlayerDataInRange(playerId, startTime, endTime);
        return ResponseEntity.ok(data);
    }
    
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteSessionData(@PathVariable String sessionId) {
        gpsDataService.deleteSessionData(sessionId);
        return ResponseEntity.ok().build();
    }
}
