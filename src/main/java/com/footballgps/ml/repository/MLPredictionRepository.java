package com.footballgps.ml.repository;

import com.footballgps.ml.model.MLPrediction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MLPredictionRepository extends MongoRepository<MLPrediction, String> {
    List<MLPrediction> findByPlayerIdOrderByPredictedAtDesc(String playerId);
    List<MLPrediction> findBySessionIdOrderByPredictedAtDesc(String sessionId);
    List<MLPrediction> findByTypeOrderByPredictedAtDesc(MLPrediction.PredictionType type);
    List<MLPrediction> findByPlayerIdAndTypeOrderByPredictedAtDesc(String playerId, MLPrediction.PredictionType type);
    List<MLPrediction> findByPredictedAtAfterOrderByPredictedAtDesc(LocalDateTime since);
    List<MLPrediction> findByConfidenceGreaterThanOrderByPredictedAtDesc(double confidence);
}
