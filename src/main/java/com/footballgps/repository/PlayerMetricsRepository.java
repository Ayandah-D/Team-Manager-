package com.footballgps.repository;

import com.footballgps.model.PlayerMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerMetricsRepository extends MongoRepository<PlayerMetrics, String> {
    List<PlayerMetrics> findByPlayerId(String playerId);
    Optional<PlayerMetrics> findByPlayerIdAndSessionId(String playerId, String sessionId);
    
    @Query("{'playerId': ?0, 'calculatedAt': {$gte: ?1, $lte: ?2}}")
    List<PlayerMetrics> findByPlayerIdAndDateRange(String playerId, LocalDateTime start, LocalDateTime end);
    
    @Query("{'sessionId': ?0}")
    List<PlayerMetrics> findBySessionId(String sessionId);
}
