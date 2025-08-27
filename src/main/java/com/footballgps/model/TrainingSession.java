package com.footballgps.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "training_sessions")
public class TrainingSession {
    @Id
    private String id;
    private String teamId;
    private String name;
    private SessionType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> playerIds;
    private String coachId;
    private SessionMetrics metrics;
    private boolean active;
    
    public enum SessionType {
        TRAINING, MATCH, RECOVERY, FITNESS_TEST
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionMetrics {
        private double averageIntensity;
        private double totalDistance;
        private int totalSprints;
        private double averageHeartRate;
        private int injuries;
    }
}
