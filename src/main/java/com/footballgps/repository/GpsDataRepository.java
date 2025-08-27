package com.footballgps.repository;

import com.footballgps.model.GpsData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GpsDataRepository extends MongoRepository<GpsData, String> {
    List<GpsData> findByPlayerIdAndSessionId(String playerId, String sessionId);
    List<GpsData> findBySessionId(String sessionId);
    
    @Query("{'playerId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<GpsData> findByPlayerIdAndTimestampBetween(String playerId, LocalDateTime start, LocalDateTime end);
    
    @Query("{'sessionId': ?0, 'timestamp': {$gte: ?1}}")
    List<GpsData> findRecentDataBySession(String sessionId, LocalDateTime since);
    
    void deleteBySessionId(String sessionId);
}
