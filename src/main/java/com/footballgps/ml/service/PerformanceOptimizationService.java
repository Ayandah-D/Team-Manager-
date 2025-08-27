package com.footballgps.ml.service;

import com.footballgps.ml.model.MLPrediction;
import com.footballgps.model.PlayerMetrics;
import com.footballgps.model.Player;
import com.footballgps.repository.PlayerMetricsRepository;
import com.footballgps.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceOptimizationService {
    
    private final PlayerMetricsRepository playerMetricsRepository;
    private final PlayerRepository playerRepository;
    private final MLPredictionService mlPredictionService;
    
    public MLPrediction optimizePerformance(String playerId) {
        log.info("Optimizing performance for player: {}", playerId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        
        Player player = playerOpt.get();
        
        // Get recent performance data (last 14 days)
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
        List<PlayerMetrics> recentMetrics = playerMetricsRepository
                .findByPlayerIdAndDateRange(playerId, twoWeeksAgo, LocalDateTime.now());
        
        if (recentMetrics.isEmpty()) {
            return createDefaultOptimizationPrediction(playerId);
        }
        
        // Analyze performance patterns
        Map<String, Object> analysis = analyzePerformancePatterns(recentMetrics, player);
        
        // Generate optimization recommendations
        Map<String, Object> optimizations = generateOptimizationRecommendations(analysis, player);
        
        // Create prediction
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setType(MLPrediction.PredictionType.PERFORMANCE_DECLINE);
        prediction.setInput(analysis);
        prediction.setOutput(optimizations);
        prediction.setConfidence(calculateOptimizationConfidence(analysis));
        prediction.setPredictedAt(LocalDateTime.now());
        
        return mlPredictionService.savePrediction(prediction);
    }
    
    private Map<String, Object> analyzePerformancePatterns(List<PlayerMetrics> metrics, Player player) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Performance trends
        List<Double> maxSpeeds = metrics.stream()
                .map(m -> m.getMovement().getMaxSpeed())
                .collect(Collectors.toList());
        
        List<Double> totalDistances = metrics.stream()
                .map(m -> m.getMovement().getTotalDistance())
                .collect(Collectors.toList());
        
        List<Double> intensityScores = metrics.stream()
                .map(m -> m.getPerformance().getIntensityScore())
                .collect(Collectors.toList());
        
        List<Double> workRates = metrics.stream()
                .map(m -> m.getPerformance().getWorkRate())
                .collect(Collectors.toList());
        
        // Calculate performance indicators
        analysis.put("avgMaxSpeed", calculateAverage(maxSpeeds));
        analysis.put("maxSpeedTrend", calculateTrend(maxSpeeds));
        analysis.put("speedConsistency", 1.0 - calculateVariability(maxSpeeds));
        
        analysis.put("avgTotalDistance", calculateAverage(totalDistances));
        analysis.put("distanceTrend", calculateTrend(totalDistances));
        analysis.put("enduranceConsistency", 1.0 - calculateVariability(totalDistances));
        
        analysis.put("avgIntensity", calculateAverage(intensityScores));
        analysis.put("intensityTrend", calculateTrend(intensityScores));
        
        analysis.put("avgWorkRate", calculateAverage(workRates));
        analysis.put("workRateTrend", calculateTrend(workRates));
        
        // Performance relative to player profile
        if (player.getProfile() != null) {
            double speedUtilization = calculateAverage(maxSpeeds) / player.getProfile().getMaxSpeed();
            analysis.put("speedUtilization", speedUtilization);
            analysis.put("fitnessLevel", player.getProfile().getFitnessLevel());
        }
        
        // Identify performance peaks and valleys
        analysis.put("performancePeaks", identifyPerformancePeaks(metrics));
        analysis.put("performanceValleys", identifyPerformanceValleys(metrics));
        
        // Recovery patterns
        analysis.put("recoveryEfficiency", calculateRecoveryEfficiency(metrics));
        
        return analysis;
    }
    
    private Map<String, Object> generateOptimizationRecommendations(Map<String, Object> analysis, Player player) {
        Map<String, Object> optimizations = new HashMap<>();
        
        List<String> recommendations = new ArrayList<>();
        List<String> trainingFocus = new ArrayList<>();
        Map<String, Object> targetMetrics = new HashMap<>();
        
        // Speed optimization
        double avgMaxSpeed = (Double) analysis.get("avgMaxSpeed");
        double maxSpeedTrend = (Double) analysis.get("maxSpeedTrend");
        
        if (maxSpeedTrend < -0.1) {
            recommendations.add("Focus on speed development - declining trend detected");
            trainingFocus.add("Sprint intervals");
            trainingFocus.add("Plyometric exercises");
        } else if (avgMaxSpeed < (player.getProfile() != null ? player.getProfile().getMaxSpeed() * 0.85 : 25.0)) {
            recommendations.add("Increase sprint training to reach speed potential");
            trainingFocus.add("Acceleration drills");
        }
        
        // Endurance optimization
        double distanceTrend = (Double) analysis.get("distanceTrend");
        double enduranceConsistency = (Double) analysis.get("enduranceConsistency");
        
        if (distanceTrend < -0.05) {
            recommendations.add("Improve aerobic capacity - endurance declining");
            trainingFocus.add("Aerobic base training");
            trainingFocus.add("Tempo runs");
        }
        
        if (enduranceConsistency < 0.8) {
            recommendations.add("Work on consistency in endurance performance");
            trainingFocus.add("Steady-state cardio");
        }
        
        // Intensity optimization
        double avgIntensity = (Double) analysis.get("avgIntensity");
        double intensityTrend = (Double) analysis.get("intensityTrend");
        
        if (avgIntensity < 6.0) {
            recommendations.add("Increase training intensity for better match preparation");
            trainingFocus.add("High-intensity intervals");
        }
        
        // Work rate optimization
        double avgWorkRate = (Double) analysis.get("avgWorkRate");
        if (avgWorkRate < 80.0) {
            recommendations.add("Improve work rate through tactical training");
            trainingFocus.add("Small-sided games");
            trainingFocus.add("Position-specific drills");
        }
        
        // Recovery optimization
        double recoveryEfficiency = (Double) analysis.get("recoveryEfficiency");
        if (recoveryEfficiency < 0.7) {
            recommendations.add("Optimize recovery protocols");
            trainingFocus.add("Active recovery sessions");
            trainingFocus.add("Sleep hygiene improvement");
        }
        
        // Position-specific recommendations
        if (player.getPosition() != null) {
            recommendations.addAll(getPositionSpecificRecommendations(player.getPosition(), analysis));
            trainingFocus.addAll(getPositionSpecificTraining(player.getPosition()));
        }
        
        // Set target metrics for improvement
        targetMetrics.put("targetMaxSpeed", avgMaxSpeed * 1.05); // 5% improvement
        targetMetrics.put("targetWorkRate", Math.min(avgWorkRate * 1.1, 95.0)); // 10% improvement, capped at 95%
        targetMetrics.put("targetIntensity", Math.min(avgIntensity * 1.15, 10.0)); // 15% improvement, capped at 10
        
        optimizations.put("recommendations", recommendations);
        optimizations.put("trainingFocus", trainingFocus);
        optimizations.put("targetMetrics", targetMetrics);
        optimizations.put("timeframe", "4-6 weeks");
        optimizations.put("priority", determinePriority(analysis));
        
        return optimizations;
    }
    
    private List<String> getPositionSpecificRecommendations(String position, Map<String, Object> analysis) {
        List<String> recommendations = new ArrayList<>();
        
        switch (position.toUpperCase()) {
            case "FORWARD":
            case "STRIKER":
                recommendations.add("Focus on explosive sprint training for breakaways");
                recommendations.add("Improve finishing under fatigue conditions");
                break;
            case "MIDFIELDER":
                recommendations.add("Enhance aerobic capacity for box-to-box play");
                recommendations.add("Work on acceleration for quick direction changes");
                break;
            case "DEFENDER":
                recommendations.add("Improve reactive speed for defensive actions");
                recommendations.add("Focus on sustained running for defensive coverage");
                break;
            case "GOALKEEPER":
                recommendations.add("Enhance explosive power for diving and jumping");
                recommendations.add("Improve agility and reaction time");
                break;
        }
        
        return recommendations;
    }
    
    private List<String> getPositionSpecificTraining(String position) {
        List<String> training = new ArrayList<>();
        
        switch (position.toUpperCase()) {
            case "FORWARD":
            case "STRIKER":
                training.add("Sprint starts from various positions");
                training.add("Finishing drills with fatigue");
                break;
            case "MIDFIELDER":
                training.add("Interval running");
                training.add("Change of direction drills");
                break;
            case "DEFENDER":
                training.add("Defensive positioning drills");
                training.add("1v1 defensive scenarios");
                break;
            case "GOALKEEPER":
                training.add("Plyometric exercises");
                training.add("Reaction time drills");
                break;
        }
        
        return training;
    }
    
    private String determinePriority(Map<String, Object> analysis) {
        double maxSpeedTrend = (Double) analysis.get("maxSpeedTrend");
        double distanceTrend = (Double) analysis.get("distanceTrend");
        double intensityTrend = (Double) analysis.get("intensityTrend");
        
        if (maxSpeedTrend < -0.15 || distanceTrend < -0.1 || intensityTrend < -0.2) {
            return "HIGH";
        } else if (maxSpeedTrend < -0.05 || distanceTrend < -0.05 || intensityTrend < -0.1) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private List<Map<String, Object>> identifyPerformancePeaks(List<PlayerMetrics> metrics) {
        List<Map<String, Object>> peaks = new ArrayList<>();
        
        // Find sessions with exceptional performance
        double avgIntensity = metrics.stream()
                .mapToDouble(m -> m.getPerformance().getIntensityScore())
                .average().orElse(0.0);
        
        double threshold = avgIntensity + (avgIntensity * 0.2); // 20% above average
        
        for (PlayerMetrics metric : metrics) {
            if (metric.getPerformance().getIntensityScore() > threshold) {
                Map<String, Object> peak = new HashMap<>();
                peak.put("date", metric.getCalculatedAt());
                peak.put("intensity", metric.getPerformance().getIntensityScore());
                peak.put("maxSpeed", metric.getMovement().getMaxSpeed());
                peak.put("workRate", metric.getPerformance().getWorkRate());
                peaks.add(peak);
            }
        }
        
        return peaks;
    }
    
    private List<Map<String, Object>> identifyPerformanceValleys(List<PlayerMetrics> metrics) {
        List<Map<String, Object>> valleys = new ArrayList<>();
        
        // Find sessions with below-average performance
        double avgIntensity = metrics.stream()
                .mapToDouble(m -> m.getPerformance().getIntensityScore())
                .average().orElse(0.0);
        
        double threshold = avgIntensity - (avgIntensity * 0.2); // 20% below average
        
        for (PlayerMetrics metric : metrics) {
            if (metric.getPerformance().getIntensityScore() < threshold) {
                Map<String, Object> valley = new HashMap<>();
                valley.put("date", metric.getCalculatedAt());
                valley.put("intensity", metric.getPerformance().getIntensityScore());
                valley.put("possibleCauses", identifyPossibleCauses(metric));
                valleys.add(valley);
            }
        }
        
        return valleys;
    }
    
    private List<String> identifyPossibleCauses(PlayerMetrics metric) {
        List<String> causes = new ArrayList<>();
        
        if (metric.getLoad().getAcuteChronicRatio() > 1.3) {
            causes.add("High training load");
        }
        
        if (metric.getLoad().getReadinessScore() < 6.0) {
            causes.add("Poor readiness/recovery");
        }
        
        if (metric.getPerformance().getMaxHeartRate() < 160) {
            causes.add("Low cardiovascular engagement");
        }
        
        return causes;
    }
    
    private double calculateRecoveryEfficiency(List<PlayerMetrics> metrics) {
        // Simplified recovery efficiency calculation
        double totalReadiness = metrics.stream()
                .mapToDouble(m -> m.getLoad().getReadinessScore())
                .sum();
        
        return totalReadiness / (metrics.size() * 10.0); // Normalize to 0-1 scale
    }
    
    // Utility methods (same as in InjuryPredictionService)
    private double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private double calculateVariability(List<Double> values) {
        double mean = calculateAverage(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance) / mean;
    }
    
    private double calculateTrend(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
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
    
    private double calculateOptimizationConfidence(Map<String, Object> analysis) {
        // Base confidence on data quality and trends clarity
        double confidence = 0.75;
        
        // Adjust based on trend strength
        double maxSpeedTrend = Math.abs((Double) analysis.get("maxSpeedTrend"));
        double distanceTrend = Math.abs((Double) analysis.get("distanceTrend"));
        
        if (maxSpeedTrend > 0.1 || distanceTrend > 0.05) {
            confidence += 0.15; // Clear trends increase confidence
        }
        
        return Math.min(0.95, confidence);
    }
    
    private MLPrediction createDefaultOptimizationPrediction(String playerId) {
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setType(MLPrediction.PredictionType.PERFORMANCE_DECLINE);
        
        Map<String, Object> output = new HashMap<>();
        output.put("recommendations", Arrays.asList(
            "Establish baseline performance data",
            "Focus on consistent training attendance",
            "Monitor basic fitness metrics"
        ));
        output.put("trainingFocus", Arrays.asList("General fitness", "Basic skills"));
        output.put("priority", "LOW");
        
        prediction.setOutput(output);
        prediction.setConfidence(0.5);
        prediction.setPredictedAt(LocalDateTime.now());
        
        return prediction;
    }
}
