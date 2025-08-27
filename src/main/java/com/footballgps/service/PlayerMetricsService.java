package com.footballgps.service;

import com.footballgps.model.PlayerMetrics;
import com.footballgps.repository.PlayerMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerMetricsService {
    
    private final PlayerMetricsRepository playerMetricsRepository;
    
    public List<PlayerMetrics> getPlayerMetrics(String playerId) {
        return playerMetricsRepository.findByPlayerId(playerId);
    }
    
    public Optional<PlayerMetrics> getPlayerSessionMetrics(String playerId, String sessionId) {
        return playerMetricsRepository.findByPlayerIdAndSessionId(playerId, sessionId);
    }
    
    public List<PlayerMetrics> getSessionMetrics(String sessionId) {
        return playerMetricsRepository.findBySessionId(sessionId);
    }
    
    public List<PlayerMetrics> getPlayerMetricsInRange(String playerId, LocalDateTime start, LocalDateTime end) {
        return playerMetricsRepository.findByPlayerIdAndDateRange(playerId, start, end);
    }
    
    public PlayerMetrics saveMetrics(PlayerMetrics metrics) {
        PlayerMetrics saved = playerMetricsRepository.save(metrics);
        log.debug("Saved metrics for player {} in session {}", 
                 metrics.getPlayerId(), metrics.getSessionId());
        return saved;
    }
}
