package com.footballgps.ml.service;

import com.footballgps.ml.model.MLPrediction;
import com.footballgps.model.PlayerMetrics;
import com.footballgps.model.GpsData;
import com.footballgps.repository.PlayerMetricsRepository;
import com.footballgps.repository.GpsDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InjuryPredictionService {
    
    private final PlayerMetricsRepository playerMetricsRepository;
    private final GpsDataRepository gpsDataRepository;
    private final MLPredictionService mlPredictionService;
    
    public MLPrediction predictInjuryRisk(String playerId) {
        log.info("Predicting injury risk for player: {}", playerId);
        
        // Get historical data (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<PlayerMetrics> historicalMetrics = playerMetricsRepository
                .findByPlayerIdAndDateRange(playerId, thirtyDaysAgo, LocalDateTime.now());
        
        if (historicalMetrics.isEmpty()) {
            log.warn("No historical data found for player: {}", playerId);
            return createLowRiskPrediction(playerId);
        }
        
        // Extract features for ML model
        Map<String, Object> features = extractInjuryRiskFeatures(historicalMetrics);
        
        // Calculate injury risk using multiple factors
        double injuryRisk = calculateInjuryRisk(features);
        
        // Create prediction
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setType(MLPrediction.PredictionType.INJURY_RISK);
        prediction.setInput(features);
        
        Map<String, Object> output = new HashMap<>();
        output.put("injuryRisk", injuryRisk);
        output.put("riskLevel", getRiskLevel(injuryRisk));
        output.put("recommendations", generateInjuryPreventionRecommendations(injuryRisk, features));
        output.put("keyFactors", identifyKeyRiskFactors(features));
        
        prediction.setOutput(output);
        prediction.setConfidence(calculateConfidence(features));
        prediction.setPredictedAt(LocalDateTime.now());
        
        return mlPredictionService.savePrediction(prediction);
    }
    
    private Map<String, Object> extractInjuryRiskFeatures(List<PlayerMetrics> metrics) {
        Map<String, Object> features = new HashMap<>();
        
        // Calculate trends and patterns
        List<Double> acuteChronicRatios = metrics.stream()
                .map(m -> m.getLoad().getAcuteChronicRatio())
                .collect(Collectors.toList());
        
        List<Double> playerLoads = metrics.stream()
                .map(m -> m.getMovement().getPlayerLoad())
                .collect(Collectors.toList());
        
        List<Double> sprintCounts = metrics.stream()
                .map(m -> (double) m.getMovement().getSprintCount())
                .collect(Collectors.toList());
        
        List<Double> maxSpeeds = metrics.stream()
                .map(m -> m.getMovement().getMaxSpeed())
                .collect(Collectors.toList());
        
        // Statistical features
        features.put("avgAcuteChronicRatio", calculateAverage(acuteChronicRatios));
        features.put("maxAcuteChronicRatio", Collections.max(acuteChronicRatios));
        features.put("acuteChronicVariability", calculateVariability(acuteChronicRatios));
        
        features.put("avgPlayerLoad", calculateAverage(playerLoads));
        features.put("playerLoadTrend", calculateTrend(playerLoads));
        features.put("playerLoadSpike", detectSpikes(playerLoads));
        
        features.put("avgSprintCount", calculateAverage(sprintCounts));
        features.put("sprintCountTrend", calculateTrend(sprintCounts));
        
        features.put("maxSpeedDecline", calculateDecline(maxSpeeds));
        features.put("speedVariability", calculateVariability(maxSpeeds));
        
        // Workload patterns
        features.put("consecutiveHighLoadDays", countConsecutiveHighLoadDays(acuteChronicRatios));
        features.put("recoveryDays", countRecoveryDays(metrics));
        features.put("workloadImbalance", calculateWorkloadImbalance(metrics));
        
        // Movement asymmetry indicators
        features.put("movementAsymmetry", calculateMovementAsymmetry(metrics));
        features.put("accelerationPatternChange", detectAccelerationPatternChanges(metrics));
        
        return features;
    }
    
    private double calculateInjuryRisk(Map<String, Object> features) {
        double risk = 0.0;
        
        // Acute:Chronic ratio risk (most important factor)
        double avgAcuteChronicRatio = (Double) features.get("avgAcuteChronicRatio");
        double maxAcuteChronicRatio = (Double) features.get("maxAcuteChronicRatio");
        
        if (maxAcuteChronicRatio > 1.5) {
            risk += 0.4; // High risk
        } else if (maxAcuteChronicRatio > 1.3) {
            risk += 0.25; // Moderate risk
        } else if (avgAcuteChronicRatio > 1.2) {
            risk += 0.1; // Low risk
        }
        
        // Player load spike risk
        boolean playerLoadSpike = (Boolean) features.get("playerLoadSpike");
        if (playerLoadSpike) {
            risk += 0.2;
        }
        
        // Consecutive high load days
        int consecutiveHighLoadDays = (Integer) features.get("consecutiveHighLoadDays");
        if (consecutiveHighLoadDays > 5) {
            risk += 0.15;
        } else if (consecutiveHighLoadDays > 3) {
            risk += 0.1;
        }
        
        // Speed decline (potential fatigue/injury indicator)
        double maxSpeedDecline = (Double) features.get("maxSpeedDecline");
        if (maxSpeedDecline > 0.1) { // 10% decline
            risk += 0.15;
        }
        
        // Movement asymmetry
        double movementAsymmetry = (Double) features.get("movementAsymmetry");
        if (movementAsymmetry > 0.15) {
            risk += 0.1;
        }
        
        return Math.min(risk, 1.0); // Cap at 1.0
    }
    
    private String getRiskLevel(double risk) {
        if (risk > 0.7) return "HIGH";
        if (risk > 0.4) return "MODERATE";
        if (risk > 0.2) return "LOW";
        return "MINIMAL";
    }
    
    private List<String> generateInjuryPreventionRecommendations(double risk, Map<String, Object> features) {
        List<String> recommendations = new ArrayList<>();
        
        if (risk > 0.7) {
            recommendations.add("URGENT: Reduce training load by 30-40% for next 7 days");
            recommendations.add("Schedule immediate medical assessment");
            recommendations.add("Focus on recovery and regeneration protocols");
        } else if (risk > 0.4) {
            recommendations.add("Reduce training intensity by 20% for next 3-5 days");
            recommendations.add("Increase recovery time between sessions");
            recommendations.add("Monitor movement patterns closely");
        } else if (risk > 0.2) {
            recommendations.add("Maintain current load but monitor closely");
            recommendations.add("Ensure adequate sleep and nutrition");
            recommendations.add("Include preventive exercises in warm-up");
        }
        
        // Specific recommendations based on features
        double avgAcuteChronicRatio = (Double) features.get("avgAcuteChronicRatio");
        if (avgAcuteChronicRatio > 1.3) {
            recommendations.add("Focus on gradual load progression");
        }
        
        double movementAsymmetry = (Double) features.get("movementAsymmetry");
        if (movementAsymmetry > 0.1) {
            recommendations.add("Address movement asymmetries with corrective exercises");
        }
        
        return recommendations;
    }
    
    private List<String> identifyKeyRiskFactors(Map<String, Object> features) {
        List<String> factors = new ArrayList<>();
        
        double maxAcuteChronicRatio = (Double) features.get("maxAcuteChronicRatio");
        if (maxAcuteChronicRatio > 1.3) {
            factors.add("Elevated acute:chronic workload ratio");
        }
        
        boolean playerLoadSpike = (Boolean) features.get("playerLoadSpike");
        if (playerLoadSpike) {
            factors.add("Recent spike in player load");
        }
        
        int consecutiveHighLoadDays = (Integer) features.get("consecutiveHighLoadDays");
        if (consecutiveHighLoadDays > 3) {
            factors.add("Consecutive high-load training days");
        }
        
        double maxSpeedDecline = (Double) features.get("maxSpeedDecline");
        if (maxSpeedDecline > 0.05) {
            factors.add("Decline in maximum speed performance");
        }
        
        return factors;
    }
    
    // Utility methods for statistical calculations
    private double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private double calculateVariability(List<Double> values) {
        double mean = calculateAverage(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance) / mean; // Coefficient of variation
    }
    
    private double calculateTrend(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        // Simple linear trend calculation
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = values.size();
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
    
    private boolean detectSpikes(List<Double> values) {
        if (values.size() < 3) return false;
        
        double mean = calculateAverage(values);
        double std = Math.sqrt(calculateVariability(values)) * mean;
        
        return values.stream().anyMatch(v -> Math.abs(v - mean) > 2 * std);
    }
    
    private double calculateDecline(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double first = values.get(0);
        double last = values.get(values.size() - 1);
        
        return (first - last) / first; // Percentage decline
    }
    
    private int countConsecutiveHighLoadDays(List<Double> ratios) {
        int maxConsecutive = 0;
        int current = 0;
        
        for (double ratio : ratios) {
            if (ratio > 1.3) {
                current++;
                maxConsecutive = Math.max(maxConsecutive, current);
            } else {
                current = 0;
            }
        }
        
        return maxConsecutive;
    }
    
    private int countRecoveryDays(List<PlayerMetrics> metrics) {
        return (int) metrics.stream()
                .filter(m -> m.getLoad().getAcuteChronicRatio() < 0.8)
                .count();
    }
    
    private double calculateWorkloadImbalance(List<PlayerMetrics> metrics) {
        List<Double> loads = metrics.stream()
                .map(m -> m.getMovement().getPlayerLoad())
                .collect(Collectors.toList());
        
        return calculateVariability(loads);
    }
    
    private double calculateMovementAsymmetry(List<PlayerMetrics> metrics) {
        // Simplified asymmetry calculation
        // In real implementation, this would analyze left vs right movement patterns
        return 0.05 + Math.random() * 0.1; // Placeholder
    }
    
    private boolean detectAccelerationPatternChanges(List<PlayerMetrics> metrics) {
        // Simplified pattern change detection
        // In real implementation, this would use more sophisticated algorithms
        return Math.random() > 0.8; // Placeholder
    }
    
    private double calculateConfidence(Map<String, Object> features) {
        // Confidence based on data quality and quantity
        double baseConfidence = 0.7;
        
        // Adjust based on data completeness
        int featureCount = features.size();
        if (featureCount > 10) {
            baseConfidence += 0.2;
        } else if (featureCount < 5) {
            baseConfidence -= 0.2;
        }
        
        return Math.max(0.5, Math.min(0.95, baseConfidence));
    }
    
    private MLPrediction createLowRiskPrediction(String playerId) {
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setType(MLPrediction.PredictionType.INJURY_RISK);
        
        Map<String, Object> output = new HashMap<>();
        output.put("injuryRisk", 0.1);
        output.put("riskLevel", "MINIMAL");
        output.put("recommendations", Arrays.asList("Continue current training regimen", "Monitor for data availability"));
        output.put("keyFactors", Arrays.asList("Insufficient historical data"));
        
        prediction.setOutput(output);
        prediction.setConfidence(0.5);
        prediction.setPredictedAt(LocalDateTime.now());
        
        return prediction;
    }
}
