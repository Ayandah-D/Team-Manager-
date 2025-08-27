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
public class TacticalAnalysisService {
    
    private final PlayerMetricsRepository playerMetricsRepository;
    private final GpsDataRepository gpsDataRepository;
    private final MLPredictionService mlPredictionService;
    
    public MLPrediction analyzeTacticalPerformance(String sessionId) {
        log.info("Analyzing tactical performance for session: {}", sessionId);
        
        // Get all players' data for the session
        List<PlayerMetrics> sessionMetrics = playerMetricsRepository.findBySessionId(sessionId);
        List<GpsData> sessionGpsData = gpsDataRepository.findBySessionId(sessionId);
        
        if (sessionMetrics.isEmpty() || sessionGpsData.isEmpty()) {
            return createDefaultTacticalAnalysis(sessionId);
        }
        
        // Analyze team tactical patterns
        Map<String, Object> tacticalAnalysis = analyzeTacticalPatterns(sessionMetrics, sessionGpsData);
        
        // Generate tactical recommendations
        Map<String, Object> recommendations = generateTacticalRecommendations(tacticalAnalysis);
        
        // Create prediction
        MLPrediction prediction = new MLPrediction();
        prediction.setSessionId(sessionId);
        prediction.setType(MLPrediction.PredictionType.TACTICAL_RECOMMENDATION);
        prediction.setInput(tacticalAnalysis);
        prediction.setOutput(recommendations);
        prediction.setConfidence(calculateTacticalConfidence(tacticalAnalysis));
        prediction.setPredictedAt(LocalDateTime.now());
        
        return mlPredictionService.savePrediction(prediction);
    }
    
    public MLPrediction predictOptimalPosition(String playerId, String sessionId) {
        log.info("Predicting optimal position for player: {} in session: {}", playerId, sessionId);
        
        List<GpsData> playerData = gpsDataRepository.findByPlayerIdAndSessionId(playerId, sessionId);
        Optional<PlayerMetrics> metricsOpt = playerMetricsRepository.findByPlayerIdAndSessionId(playerId, sessionId);
        
        if (playerData.isEmpty() || metricsOpt.isEmpty()) {
            return createDefaultPositionPrediction(playerId);
        }
        
        PlayerMetrics metrics = metricsOpt.get();
        
        // Analyze movement patterns and positioning
        Map<String, Object> positionAnalysis = analyzePositionalPlay(playerData, metrics);
        
        // Predict optimal position
        Map<String, Object> positionRecommendation = predictOptimalPositioning(positionAnalysis);
        
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setSessionId(sessionId);
        prediction.setType(MLPrediction.PredictionType.OPTIMAL_POSITION);
        prediction.setInput(positionAnalysis);
        prediction.setOutput(positionRecommendation);
        prediction.setConfidence(calculatePositionConfidence(positionAnalysis));
        prediction.setPredictedAt(LocalDateTime.now());
        
        return mlPredictionService.savePrediction(prediction);
    }
    
    private Map<String, Object> analyzeTacticalPatterns(List<PlayerMetrics> metrics, List<GpsData> gpsData) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Team formation analysis
        Map<String, Object> formation = analyzeFormation(metrics);
        analysis.put("formation", formation);
        
        // Team compactness
        double compactness = calculateTeamCompactness(gpsData);
        analysis.put("teamCompactness", compactness);
        
        // Pressing intensity
        Map<String, Object> pressing = analyzePressingPatterns(metrics);
        analysis.put("pressing", pressing);
        
        // Ball possession patterns (simulated)
        Map<String, Object> possession = analyzePossessionPatterns(gpsData);
        analysis.put("possession", possession);
        
        // Transition analysis
        Map<String, Object> transitions = analyzeTransitions(gpsData);
        analysis.put("transitions", transitions);
        
        // Defensive organization
        Map<String, Object> defense = analyzeDefensiveOrganization(metrics);
        analysis.put("defense", defense);
        
        // Attacking patterns
        Map<String, Object> attack = analyzeAttackingPatterns(metrics);
        analysis.put("attack", attack);
        
        return analysis;
    }
    
    private Map<String, Object> analyzeFormation(List<PlayerMetrics> metrics) {
        Map<String, Object> formation = new HashMap<>();
        
        // Calculate average positions for each player
        Map<String, double[]> avgPositions = new HashMap<>();
        for (PlayerMetrics metric : metrics) {
            if (metric.getTactical() != null) {
                double[] pos = {metric.getTactical().getAveragePositionX(), 
                              metric.getTactical().getAveragePositionY()};
                avgPositions.put(metric.getPlayerId(), pos);
            }
        }
        
        // Identify formation pattern
        String detectedFormation = identifyFormation(avgPositions);
        formation.put("detectedFormation", detectedFormation);
        formation.put("formationStability", calculateFormationStability(metrics));
        formation.put("playerPositions", avgPositions);
        
        return formation;
    }
    
    private String identifyFormation(Map<String, double[]> positions) {
        // Simplified formation detection
        // In real implementation, this would use clustering algorithms
        
        if (positions.size() < 7) return "Unknown";
        
        // Group players by field zones
        int defenders = 0, midfielders = 0, forwards = 0;
        
        for (double[] pos : positions.values()) {
            double y = pos[1]; // Y coordinate represents field position
            
            if (y < 33) defenders++;
            else if (y < 67) midfielders++;
            else forwards++;
        }
        
        return defenders + "-" + midfielders + "-" + forwards;
    }
    
    private double calculateFormationStability(List<PlayerMetrics> metrics) {
        // Calculate how well players maintain their positions
        double totalAdherence = metrics.stream()
                .filter(m -> m.getTactical() != null)
                .mapToDouble(m -> m.getTactical().getFormationAdherence())
                .average().orElse(0.0);
        
        return totalAdherence / 100.0; // Convert to 0-1 scale
    }
    
    private double calculateTeamCompactness(List<GpsData> gpsData) {
        // Group data by timestamp to analyze team shape at each moment
        Map<LocalDateTime, List<GpsData>> timeGroups = gpsData.stream()
                .collect(Collectors.groupingBy(GpsData::getTimestamp));
        
        double avgCompactness = 0.0;
        int validMeasurements = 0;
        
        for (List<GpsData> timeGroup : timeGroups.values()) {
            if (timeGroup.size() >= 7) { // Need minimum players for meaningful measurement
                double compactness = calculateMomentaryCompactness(timeGroup);
                avgCompactness += compactness;
                validMeasurements++;
            }
        }
        
        return validMeasurements > 0 ? avgCompactness / validMeasurements : 0.0;
    }
    
    private double calculateMomentaryCompactness(List<GpsData> playerPositions) {
        // Calculate the area covered by the team
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        for (GpsData data : playerPositions) {
            double x = data.getPosition().getLongitude();
            double y = data.getPosition().getLatitude();
            
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        
        double area = (maxX - minX) * (maxY - minY);
        return 1.0 / (1.0 + area); // Inverse relationship - smaller area = higher compactness
    }
    
    private Map<String, Object> analyzePressingPatterns(List<PlayerMetrics> metrics) {
        Map<String, Object> pressing = new HashMap<>();
        
        // Calculate team pressing intensity
        double avgIntensity = metrics.stream()
                .filter(m -> m.getPerformance() != null)
                .mapToDouble(m -> m.getPerformance().getIntensityScore())
                .average().orElse(0.0);
        
        pressing.put("averageIntensity", avgIntensity);
        pressing.put("pressingEffectiveness", calculatePressingEffectiveness(metrics));
        pressing.put("pressingTriggers", identifyPressingTriggers(metrics));
        
        return pressing;
    }
    
    private double calculatePressingEffectiveness(List<PlayerMetrics> metrics) {
        // Simplified effectiveness calculation
        // In real implementation, this would correlate with ball recovery data
        double avgWorkRate = metrics.stream()
                .filter(m -> m.getPerformance() != null)
                .mapToDouble(m -> m.getPerformance().getWorkRate())
                .average().orElse(0.0);
        
        return avgWorkRate / 100.0;
    }
    
    private List<String> identifyPressingTriggers(List<PlayerMetrics> metrics) {
        List<String> triggers = new ArrayList<>();
        
        // Analyze when pressing occurs (simplified)
        double avgAccelerations = metrics.stream()
                .filter(m -> m.getMovement() != null)
                .mapToDouble(m -> m.getMovement().getAccelerationCount())
                .average().orElse(0.0);
        
        if (avgAccelerations > 20) {
            triggers.add("High ball recovery attempts");
        }
        
        triggers.add("Opponent ball possession in middle third");
        triggers.add("Slow opponent build-up play");
        
        return triggers;
    }
    
    private Map<String, Object> analyzePossessionPatterns(List<GpsData> gpsData) {
        Map<String, Object> possession = new HashMap<>();
        
        // Simulated possession analysis
        possession.put("averagePossessionTime", 45.0); // seconds
        possession.put("possessionZones", Arrays.asList("Defensive third: 35%", "Middle third: 45%", "Attacking third: 20%"));
        possession.put("buildUpSpeed", "Medium");
        
        return possession;
    }
    
    private Map<String, Object> analyzeTransitions(List<GpsData> gpsData) {
        Map<String, Object> transitions = new HashMap<>();
        
        // Analyze speed of transitions
        double avgTransitionSpeed = gpsData.stream()
                .filter(data -> data.getMovement().getSpeed() > 15.0) // High-speed movements
                .mapToDouble(data -> data.getMovement().getSpeed())
                .average().orElse(0.0);
        
        transitions.put("averageTransitionSpeed", avgTransitionSpeed);
        transitions.put("transitionEfficiency", calculateTransitionEfficiency(gpsData));
        transitions.put("counterAttackFrequency", "Medium");
        
        return transitions;
    }
    
    private double calculateTransitionEfficiency(List<GpsData> gpsData) {
        // Simplified calculation based on speed and direction changes
        long highSpeedMovements = gpsData.stream()
                .filter(data -> data.getMovement().getSpeed() > 20.0)
                .count();
        
        return Math.min(1.0, highSpeedMovements / (double) gpsData.size() * 10);
    }
    
    private Map<String, Object> analyzeDefensiveOrganization(List<PlayerMetrics> metrics) {
        Map<String, Object> defense = new HashMap<>();
        
        double avgFormationAdherence = metrics.stream()
                .filter(m -> m.getTactical() != null)
                .mapToDouble(m -> m.getTactical().getFormationAdherence())
                .average().orElse(0.0);
        
        defense.put("organizationLevel", avgFormationAdherence);
        defense.put("defensiveCompactness", "Good");
        defense.put("pressingCoordination", calculatePressingCoordination(metrics));
        
        return defense;
    }
    
    private double calculatePressingCoordination(List<PlayerMetrics> metrics) {
        // Calculate how synchronized the team's pressing is
        double avgSynchronization = metrics.stream()
                .filter(m -> m.getTactical() != null)
                .mapToDouble(m -> m.getTactical().getTeamSynchronization())
                .average().orElse(0.0);
        
        return avgSynchronization / 100.0;
    }
    
    private Map<String, Object> analyzeAttackingPatterns(List<PlayerMetrics> metrics) {
        Map<String, Object> attack = new HashMap<>();
        
        double avgSprintDistance = metrics.stream()
                .filter(m -> m.getMovement() != null)
                .mapToDouble(m -> m.getMovement().getSprintDistance())
                .average().orElse(0.0);
        
        attack.put("attackingIntensity", avgSprintDistance / 1000.0); // Convert to km
        attack.put("widthUtilization", "Good");
        attack.put("penetrationAttempts", "Medium");
        
        return attack;
    }
    
    private Map<String, Object> generateTacticalRecommendations(Map<String, Object> analysis) {
        Map<String, Object> recommendations = new HashMap<>();
        List<String> tacticalAdvice = new ArrayList<>();
        List<String> trainingFocus = new ArrayList<>();
        
        // Formation recommendations
        @SuppressWarnings("unchecked")
        Map<String, Object> formation = (Map<String, Object>) analysis.get("formation");
        double formationStability = (Double) formation.get("formationStability");
        
        if (formationStability < 0.7) {
            tacticalAdvice.add("Improve formation discipline - players drifting from positions");
            trainingFocus.add("Positional play drills");
        }
        
        // Compactness recommendations
        double teamCompactness = (Double) analysis.get("teamCompactness");
        if (teamCompactness < 0.6) {
            tacticalAdvice.add("Increase team compactness - too much space between lines");
            trainingFocus.add("Compactness drills");
        }
        
        // Pressing recommendations
        @SuppressWarnings("unchecked")
        Map<String, Object> pressing = (Map<String, Object>) analysis.get("pressing");
        double pressingEffectiveness = (Double) pressing.get("pressingEffectiveness");
        
        if (pressingEffectiveness < 0.7) {
            tacticalAdvice.add("Improve pressing coordination and timing");
            trainingFocus.add("Pressing triggers training");
        }
        
        // Transition recommendations
        @SuppressWarnings("unchecked")
        Map<String, Object> transitions = (Map<String, Object>) analysis.get("transitions");
        double transitionEfficiency = (Double) transitions.get("transitionEfficiency");
        
        if (transitionEfficiency < 0.6) {
            tacticalAdvice.add("Work on faster transitions between phases");
            trainingFocus.add("Transition speed drills");
        }
        
        recommendations.put("tacticalAdvice", tacticalAdvice);
        recommendations.put("trainingFocus", trainingFocus);
        recommendations.put("priority", determineTacticalPriority(analysis));
        recommendations.put("expectedImprovement", "15-25% within 3-4 training sessions");
        
        return recommendations;
    }
    
    private String determineTacticalPriority(Map<String, Object> analysis) {
        double teamCompactness = (Double) analysis.get("teamCompactness");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> formation = (Map<String, Object>) analysis.get("formation");
        double formationStability = (Double) formation.get("formationStability");
        
        if (teamCompactness < 0.5 || formationStability < 0.6) {
            return "HIGH";
        } else if (teamCompactness < 0.7 || formationStability < 0.8) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private Map<String, Object> analyzePositionalPlay(List<GpsData> playerData, PlayerMetrics metrics) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Calculate position heat map
        Map<String, Integer> heatMap = calculatePositionHeatMap(playerData);
        analysis.put("heatMap", heatMap);
        
        // Analyze movement patterns
        analysis.put("averagePosition", Arrays.asList(
            metrics.getTactical().getAveragePositionX(),
            metrics.getTactical().getAveragePositionY()
        ));
        
        analysis.put("fieldCoverage", metrics.getTactical().getFieldCoverage());
        analysis.put("movementVariability", calculateMovementVariability(playerData));
        
        return analysis;
    }
    
    private Map<String, Integer> calculatePositionHeatMap(List<GpsData> playerData) {
        Map<String, Integer> heatMap = new HashMap<>();
        
        // Divide field into zones and count time spent in each
        for (GpsData data : playerData) {
            String zone = getFieldZone(data.getPosition().getLongitude(), data.getPosition().getLatitude());
            heatMap.put(zone, heatMap.getOrDefault(zone, 0) + 1);
        }
        
        return heatMap;
    }
    
    private String getFieldZone(double x, double y) {
        // Simplified zone calculation (3x3 grid)
        int zoneX = (int) (x / 33.33) + 1;
        int zoneY = (int) (y / 33.33) + 1;
        return "zone_" + Math.min(zoneX, 3) + "_" + Math.min(zoneY, 3);
    }
    
    private double calculateMovementVariability(List<GpsData> playerData) {
        // Calculate how much the player moves around their average position
        double avgX = playerData.stream().mapToDouble(d -> d.getPosition().getLongitude()).average().orElse(0.0);
        double avgY = playerData.stream().mapToDouble(d -> d.getPosition().getLatitude()).average().orElse(0.0);
        
        double variance = playerData.stream()
                .mapToDouble(d -> Math.pow(d.getPosition().getLongitude() - avgX, 2) + 
                               Math.pow(d.getPosition().getLatitude() - avgY, 2))
                .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private Map<String, Object> predictOptimalPositioning(Map<String, Object> analysis) {
        Map<String, Object> recommendation = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Double> avgPosition = (List<Double>) analysis.get("averagePosition");
        double fieldCoverage = (Double) analysis.get("fieldCoverage");
        
        // Recommend position adjustments
        List<String> adjustments = new ArrayList<>();
        
        if (fieldCoverage < 60.0) {
            adjustments.add("Increase field coverage - move more dynamically");
        }
        
        if (avgPosition.get(1) < 30) { // Too defensive
            adjustments.add("Push higher up the field when team has possession");
        } else if (avgPosition.get(1) > 70) { // Too attacking
            adjustments.add("Drop deeper to help with build-up play");
        }
        
        recommendation.put("positionAdjustments", adjustments);
        recommendation.put("optimalZone", determineOptimalZone(analysis));
        recommendation.put("movementPattern", "Dynamic with structured positioning");
        
        return recommendation;
    }
    
    private String determineOptimalZone(Map<String, Object> analysis) {
        @SuppressWarnings("unchecked")
        List<Double> avgPosition = (List<Double>) analysis.get("averagePosition");
        
        double x = avgPosition.get(0);
        double y = avgPosition.get(1);
        
        if (y < 33) return "Defensive third";
        else if (y < 67) return "Middle third";
        else return "Attacking third";
    }
    
    private double calculateTacticalConfidence(Map<String, Object> analysis) {
        // Base confidence on data completeness and pattern clarity
        double confidence = 0.8;
        
        double teamCompactness = (Double) analysis.get("teamCompactness");
        if (teamCompactness > 0.7) {
            confidence += 0.1; // Clear patterns increase confidence
        }
        
        return Math.min(0.95, confidence);
    }
    
    private double calculatePositionConfidence(Map<String, Object> analysis) {
        double fieldCoverage = (Double) analysis.get("fieldCoverage");
        double movementVariability = (Double) analysis.get("movementVariability");
        
        double confidence = 0.75;
        
        if (fieldCoverage > 70.0 && movementVariability > 0.1) {
            confidence += 0.15; // Good coverage and movement patterns
        }
        
        return Math.min(0.9, confidence);
    }
    
    private MLPrediction createDefaultTacticalAnalysis(String sessionId) {
        MLPrediction prediction = new MLPrediction();
        prediction.setSessionId(sessionId);
        prediction.setType(MLPrediction.PredictionType.TACTICAL_RECOMMENDATION);
        
        Map<String, Object> output = new HashMap<>();
        output.put("tacticalAdvice", Arrays.asList("Insufficient data for tactical analysis"));
        output.put("trainingFocus", Arrays.asList("Basic positioning", "Team shape"));
        output.put("priority", "LOW");
        
        prediction.setOutput(output);
        prediction.setConfidence(0.3);
        prediction.setPredictedAt(LocalDateTime.now());
        
        return prediction;
    }
    
    private MLPrediction createDefaultPositionPrediction(String playerId) {
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setType(MLPrediction.PredictionType.OPTIMAL_POSITION);
        
        Map<String, Object> output = new HashMap<>();
        output.put("positionAdjustments", Arrays.asList("Maintain current position", "Focus on consistency"));
        output.put("optimalZone", "Current zone");
        
        prediction.setOutput(output);
        prediction.setConfidence(0.4);
        prediction.setPredictedAt(LocalDateTime.now());
        
        return prediction;
    }
}
