package com.footballgps.service;

import com.footballgps.model.GpsData;
import com.footballgps.repository.GpsDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsDataService {
    
    private final GpsDataRepository gpsDataRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsCalculationService metricsCalculationService;
    
    public GpsData saveGpsData(GpsData gpsData) {
        gpsData.setTimestamp(LocalDateTime.now());
        GpsData saved = gpsDataRepository.save(gpsData);
        
        // Send real-time update to connected clients
        messagingTemplate.convertAndSend("/topic/gps/" + gpsData.getSessionId(), saved);
        
        // Trigger metrics calculation asynchronously
        metricsCalculationService.calculateRealTimeMetrics(saved);
        
        log.debug("GPS data saved for player {} in session {}", 
                 gpsData.getPlayerId(), gpsData.getSessionId());
        
        return saved;
    }
    
    public List<GpsData> getSessionData(String sessionId) {
        return gpsDataRepository.findBySessionId(sessionId);
    }
    
    public List<GpsData> getPlayerSessionData(String playerId, String sessionId) {
        return gpsDataRepository.findByPlayerIdAndSessionId(playerId, sessionId);
    }
    
    public List<GpsData> getRecentData(String sessionId, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return gpsDataRepository.findRecentDataBySession(sessionId, since);
    }
    
    public List<GpsData> getPlayerDataInRange(String playerId, LocalDateTime start, LocalDateTime end) {
        return gpsDataRepository.findByPlayerIdAndTimestampBetween(playerId, start, end);
    }
    
    public void deleteSessionData(String sessionId) {
        gpsDataRepository.deleteBySessionId(sessionId);
        log.info("Deleted GPS data for session {}", sessionId);
    }
}
