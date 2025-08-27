package com.footballgps.ml.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ml_predictions")
public class MLPrediction {
    @Id
    private String id;
    private String playerId;
    private String sessionId;
    private String modelId;
    private PredictionType type;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private double confidence;
    private LocalDateTime predictedAt;
    private boolean isActual; // for model validation
    private Map<String, Object> metadata;
    
    public enum PredictionType {
        INJURY_RISK,
        PERFORMANCE_DECLINE,
        OPTIMAL_POSITION,
        FATIGUE_LEVEL,
        RECOVERY_TIME,
        TACTICAL_RECOMMENDATION
    }
}
