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
@Document(collection = "ml_models")
public class MLModel {
    @Id
    private String id;
    private String name;
    private ModelType type;
    private String version;
    private ModelStatus status;
    private Map<String, Object> parameters;
    private Map<String, Double> metrics; // accuracy, precision, recall, etc.
    private LocalDateTime trainedAt;
    private LocalDateTime lastUsed;
    private String modelPath;
    private Map<String, Object> metadata;
    
    public enum ModelType {
        INJURY_PREDICTION,
        PERFORMANCE_OPTIMIZATION,
        TACTICAL_ANALYSIS,
        FATIGUE_DETECTION,
        MOVEMENT_PATTERN_RECOGNITION,
        LOAD_MANAGEMENT,
        POSITION_CLASSIFICATION
    }
    
    public enum ModelStatus {
        TRAINING,
        READY,
        DEPRECATED,
        FAILED
    }
}
