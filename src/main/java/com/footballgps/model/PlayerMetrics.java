package com.footballgps.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "player_metrics")
public class PlayerMetrics {
    @Id
    private String id;
    private String playerId;
    private String sessionId;
    private LocalDateTime calculatedAt;
    private MovementMetrics movement;
    private PerformanceMetrics performance;
    private TacticalMetrics tactical;
    private LoadMetrics load;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovementMetrics {
        private double totalDistance; // meters
        private double sprintDistance; // meters (>24 km/h)
        private double highIntensityDistance; // meters (19.8-24 km/h)
        private int sprintCount;
        private int accelerationCount; // >3 m/s²
        private int decelerationCount; // <-3 m/s²
        private double maxSpeed; // km/h
        private double averageSpeed; // km/h
        private int jumpCount;
        private double playerLoad; // cumulative impact
        private Map<String, Double> speedZones; // zone -> distance
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private double workRate; // %
        private double intensityScore; // 1-10
        private double fatigueIndex; // 1-10
        private double recoveryTime; // seconds
        private int maxHeartRate;
        private int averageHeartRate;
        private double vo2Max; // estimated
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TacticalMetrics {
        private double fieldCoverage; // %
        private Map<String, Double> heatMap; // zone -> time spent
        private double formationAdherence; // %
        private double teamSynchronization; // %
        private int passingNetworkConnections;
        private double averagePositionX; // field coordinates
        private double averagePositionY; // field coordinates
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoadMetrics {
        private double acuteLoad; // 7-day rolling average
        private double chronicLoad; // 28-day rolling average
        private double acuteChronicRatio; // injury risk indicator
        private double trainingStressScore;
        private int recoveryHours;
        private double readinessScore; // 1-10
    }
}
