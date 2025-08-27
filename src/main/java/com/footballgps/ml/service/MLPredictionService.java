package com.footballgps.ml.service;

import com.footballgps.ml.model.MLPrediction;
import com.footballgps.ml.repository.MLPredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLPredictionService {
    
    private final MLPredictionRepository mlPredictionRepository;
    
    public MLPrediction savePrediction(MLPrediction prediction) {
        MLPrediction saved = mlPredictionRepository.save(prediction);
        log.info("Saved ML prediction: {} for player: {}", 
                prediction.getType(), prediction.getPlayerId());
        return saved;
    }
    
    public List<MLPrediction> getPlayerPredictions(String playerId) {
        return mlPredictionRepository.findByPlayerIdOrderByPredictedAtDesc(playerId);
    }
    
    public List<MLPrediction> getSessionPredictions(String sessionId) {
        return mlPredictionRepository.findBySessionIdOrderByPredictedAtDesc(sessionId);
    }
    
    public List<MLPrediction> getPredictionsByType(MLPrediction.PredictionType type) {
        return mlPredictionRepository.findByTypeOrderByPredictedAtDesc(type);
    }
    
    public Optional<MLPrediction> getLatestPrediction(String playerId, MLPrediction.PredictionType type) {
        List<MLPrediction> predictions = mlPredictionRepository
                .findByPlayerIdAndTypeOrderByPredictedAtDesc(playerId, type);
        return predictions.isEmpty() ? Optional.empty() : Optional.of(predictions.get(0));
    }
    
    public List<MLPrediction> getRecentPredictions(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return mlPredictionRepository.findByPredictedAtAfterOrderByPredictedAtDesc(since);
    }
    
    public void deletePrediction(String id) {
        mlPredictionRepository.deleteById(id);
        log.info("Deleted ML prediction: {}", id);
    }
}
