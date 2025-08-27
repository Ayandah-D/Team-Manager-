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
public class FatigueDetectionService {
    
    private final PlayerMetricsRepository playerMetricsRepository;
    private final GpsDataRepository gpsDataRepository;
    private final MLPredictionService mlPredictionService;
    
    public MLPrediction detectFatigue(String playerId, String sessionId) {
        log.info("Detecting fatigue for player: {} in session: {}", playerId, sessionId);
        
        List<GpsData> sessionData = gpsDataRepository.findByPlayerIdAndSessionId(playerId, sessionId);
        Optional<PlayerMetrics> metricsOpt = playerMetricsRepository.findByPlayerIdAndSessionId(playerId, sessionId);
        
        if (sessionData.isEmpty()) {
            return createDefaultFatiguePrediction(playerId);
        }
        
        // Analyze fatigue indicators
        Map<String, Object> fatigueAnalysis = analyzeFatigueIndicators(sessionData, metricsOpt.orElse(null));
        
        // Calculate fatigue level
        double fatigueLevel = calculateFatigueLevel(fatigueAnalysis);
        
        // Generate fatigue management recommendations
        Map<String, Object> recommendations = generateFatigueRecommendations(fatigueLevel, fatigueAnalysis);
        
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setSessionId(sessionId);
        prediction.setType(MLPrediction.PredictionType.FATIGUE_LEVEL);
        prediction.setInput(fatigueAnalysis);
        prediction.setOutput(recommendations);
        prediction.setConfidence(calculateFatigueConfidence(fatigueAnalysis));
        prediction.setPredictedAt(LocalDateTime.now());
        
        return mlPredictionService.savePrediction(prediction);
    }
    
    private Map<String, Object> analyzeFatigueIndicators(List<GpsData> sessionData, PlayerMetrics metrics) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Sort data by timestamp
        sessionData.sort(Comparator.comparing(GpsData::getTimestamp));
        
        // Analyze speed decline over time
        Map<String, Double> speedDecline = analyzeSpeedDecline(sessionData);
        analysis.put("speedDecline", speedDecline);
        
        // Analyze heart rate patterns
        Map<String, Object> heartRateAnalysis = analyzeHeartRatePatterns(sessionData);
        analysis.put("heartRate", heartRateAnalysis);
        
        // Analyze movement efficiency
        double movementEfficiency = calculateMovementEfficiency(sessionData);
        analysis.put("movementEfficiency", movementEfficiency);
        
        // Analyze acceleration patterns
        Map<String, Object> accelerationAnalysis = analyzeAccelerationPatterns(sessionData);
        analysis.put("acceleration", accelerationAnalysis);
        
        // Analyze recovery between high-intensity efforts
        List<Double> recoveryTimes = calculateRecoveryTimes(sessionData);
        analysis.put("recoveryTimes", recoveryTimes);
        
        // Player load accumulation
        if (metrics != null) {
            analysis.put("playerLoad", metrics.getMovement().getPlayerLoad());
            analysis.put("workRate", metrics.getPerformance().getWorkRate());
        }
        
        return analysis;
    }
    
    private Map<String, Double> analyzeSpeedDecline(List<GpsData> sessionData) {
        Map<String, Double> speedAnalysis = new HashMap<>();
        
        // Divide session into quarters
        int quarterSize = sessionData.size() / 4;
        
        List<Double> q1Speeds = sessionData.subList(0, quarterSize).stream()
                .map(d -> d.getMovement().getSpeed())
                .collect(Collectors.toList());
        
        List<Double> q4Speeds = sessionData.subList(3 * quarterSize, sessionData.size()).stream()
                .map(d -> d.getMovement().getSpeed())
                .collect(Collectors.toList());
        
        double q1AvgSpeed = q1Speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double q4AvgSpeed = q4Speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double speedDecline = (q1AvgSpeed - q4AvgSpeed) / q1AvgSpeed;
        
        speedAnalysis.put("firstQuarterSpeed", q1AvgSpeed);
        speedAnalysis.put("lastQuarterSpeed", q4AvgSpeed);
        speedAnalysis.put("speedDeclinePercentage", speedDecline * 100);
        
        return speedAnalysis;
    }
    
    private Map<String, Object> analyzeHeartRatePatterns(List<GpsData> sessionData) {
        Map<String, Object> hrAnalysis = new HashMap<>();
        
        List<Integer> heartRates = sessionData.stream()
                .filter(d -> d.getBiometrics() != null && d.getBiometrics().getHeartRate() > 0)
                .map(d -> d.getBiometrics().getHeartRate())
                .collect(Collectors.toList());
        
        if (heartRates.isEmpty()) {
            hrAnalysis.put("available", false);
            return hrAnalysis;
        }
        
        hrAnalysis.put("available", true);
        hrAnalysis.put("averageHR", heartRates.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        hrAnalysis.put("maxHR", heartRates.stream().mapToInt(Integer::intValue).max().orElse(0));
        hrAnalysis.put("hrVariability", calculateHRVariability(heartRates));
        hrAnalysis.put("timeInZone4Plus", calculateTimeInHighHRZones(heartRates));
        
        return hrAnalysis;
    }
    
    private double calculateHRVariability(List<Integer> heartRates) {
        if (heartRates.size() < 2) return 0.0;
        
        double mean = heartRates.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = heartRates.stream()
                .mapToDouble(hr -> Math.pow(hr - mean, 2))
                .average().orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateTimeInHighHRZones(List<Integer> heartRates) {
        // Assume Zone 4+ is >85% of max HR (estimated at 190 for simplicity)
        int zone4Threshold = (int) (190 * 0.85);
        
        long highHRCount = heartRates.stream()
                .filter(hr -> hr > zone4Threshold)
                .count();
        
        return (double) highHRCount / heartRates.size() * 100;
    }
    
    private double calculateMovementEfficiency(List<GpsData> sessionData) {
        // Calculate ratio of distance covered to energy expended (simplified)
        double totalDistance = 0.0;
        double totalAcceleration = 0.0;
        
        for (int i = 1; i < sessionData.size(); i++) {
            GpsData current = sessionData.get(i);
            GpsData previous = sessionData.get(i - 1);
            
            // Calculate distance
            double distance = calculateDistance(
                previous.getPosition().getLatitude(), previous.getPosition().getLongitude(),
                current.getPosition().getLatitude(), current.getPosition().getLongitude()
            );
            
            totalDistance += distance;
            totalAcceleration += Math.abs(current.getMovement().getAcceleration());
        }
        
        return totalAcceleration > 0 ? totalDistance / totalAcceleration : 0.0;
    }
    
    private Map<String, Object> analyzeAccelerationPatterns(List<GpsData> sessionData) {
        Map<String, Object> accAnalysis = new HashMap<>();
        
        List<Double> accelerations = sessionData.stream()
                .map(d -> Math.abs(d.getMovement().getAcceleration()))
                .collect(Collectors.toList());
        
        // Analyze decline in acceleration capacity
        int quarterSize = accelerations.size() / 4;
        
        double q1AvgAcc = accelerations.subList(0, quarterSize).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double q4AvgAcc = accelerations.subList(3 * quarterSize, accelerations.size()).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double accDecline = (q1AvgAcc - q4AvgAcc) / q1AvgAcc;
        
        accAnalysis.put("firstQuarterAcceleration", q1AvgAcc);
        accAnalysis.put("lastQuarterAcceleration", q4AvgAcc);
        accAnalysis.put("accelerationDecline", accDecline * 100);
        
        return accAnalysis;
    }
    
    private List<Double> calculateRecoveryTimes(List<GpsData> sessionData) {
        List<Double> recoveryTimes = new ArrayList<>();
        
        boolean inHighIntensity = false;
        LocalDateTime highIntensityStart = null;
        
        for (GpsData data : sessionData) {
            boolean isHighIntensity = data.getMovement().getSpeed() > 20.0 || 
                                    Math.abs(data.getMovement().getAcceleration()) > 3.0;
            
            if (isHighIntensity && !inHighIntensity) {
                // Start of high-intensity period
                inHighIntensity = true;
                highIntensityStart = data.getTimestamp();
            } else if (!isHighIntensity && inHighIntensity && highIntensityStart != null) {
                // End of high-intensity period, start measuring recovery
                inHighIntensity = false;
                
                // Find next high-intensity period to calculate recovery time
                for (int i = sessionData.indexOf(data) + 1; i < sessionData.size(); i++) {
                    GpsData nextData = sessionData.get(i);
                    boolean nextIsHighIntensity = nextData.getMovement().getSpeed() > 20.0 || 
                                                Math.abs(nextData.getMovement().getAcceleration()) > 3.0;
                    
                    if (nextIsHighIntensity) {
                        double recoverySeconds = java.time.Duration.between(
                            data.getTimestamp(), nextData.getTimestamp()).getSeconds();
                        recoveryTimes.add(recoverySeconds);
                        break;
                    }
                }
            }
        }
        
        return recoveryTimes;
    }
    
    private double calculateFatigueLevel(Map<String, Object> analysis) {
        double fatigueScore = 0.0;
        
        // Speed decline factor (30% weight)
        @SuppressWarnings("unchecked")
        Map<String, Double> speedDecline = (Map<String, Double>) analysis.get("speedDecline");
        double speedDeclinePercent = speedDecline.get("speedDeclinePercentage");
        
        if (speedDeclinePercent > 15) {
            fatigueScore += 0.3;
        } else if (speedDeclinePercent > 10) {
            fatigueScore += 0.2;
        } else if (speedDeclinePercent > 5) {
            fatigueScore += 0.1;
        }
        
        // Heart rate patterns (25% weight)
        @SuppressWarnings("unchecked")
        Map<String, Object> heartRate = (Map<String, Object>) analysis.get("heartRate");
        if ((Boolean) heartRate.get("available")) {
            double timeInHighZones = (Double) heartRate.get("timeInZone4Plus");
            if (timeInHighZones > 60) {
                fatigueScore += 0.25;
            } else if (timeInHighZones > 40) {
                fatigueScore += 0.15;
            }
        }
        
        // Movement efficiency (20% weight)
        double movementEfficiency = (Double) analysis.get("movementEfficiency");
        if (movementEfficiency < 50) {
            fatigueScore += 0.2;
        } else if (movementEfficiency < 75) {
            fatigueScore += 0.1;
        }
        
        // Acceleration decline (15% weight)
        @SuppressWarnings("unchecked")
        Map<String, Object> acceleration = (Map<String, Object>) analysis.get("acceleration");
        double accDecline = (Double) acceleration.get("accelerationDecline");
        
        if (accDecline > 20) {
            fatigueScore += 0.15;
        } else if (accDecline > 10) {
            fatigueScore += 0.1;
        }
        
        // Recovery times (10% weight)
        @SuppressWarnings("unchecked")
        List<Double> recoveryTimes = (List<Double>) analysis.get("recoveryTimes");
        if (!recoveryTimes.isEmpty()) {
            double avgRecovery = recoveryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            if (avgRecovery > 120) { // More than 2 minutes
                fatigueScore += 0.1;
            } else if (avgRecovery > 90) {
                fatigueScore += 0.05;
            }
        }
        
        return Math.min(1.0, fatigueScore);
    }
    
    private Map<String, Object> generateFatigueRecommendations(double fatigueLevel, Map<String, Object> analysis) {
        Map<String, Object> recommendations = new HashMap<>();
        List<String> actions = new ArrayList<>();
        List<String> recoveryProtocols = new ArrayList<>();
        
        String fatigueCategory = getFatigueCategory(fatigueLevel);
        recommendations.put("fatigueLevel", fatigueLevel);
        recommendations.put("fatigueCategory", fatigueCategory);
        
        switch (fatigueCategory) {
            case "SEVERE":
                actions.add("IMMEDIATE SUBSTITUTION RECOMMENDED");
                actions.add("Complete rest for remainder of session");
                actions.add("Medical assessment required");
                recoveryProtocols.add("Ice bath therapy");
                recoveryProtocols.add("Extended sleep (9+ hours)");
                recoveryProtocols.add("Nutritional recovery plan");
                break;
                
            case "HIGH":
                actions.add("Consider substitution in next 10-15 minutes");
                actions.add("Reduce intensity - avoid high-speed runs");
                actions.add("Monitor closely for further decline");
                recoveryProtocols.add("Active recovery protocols");
                recoveryProtocols.add("Hydration focus");
                recoveryProtocols.add("Light stretching");
                break;
                
            case "MODERATE":
                actions.add("Manage workload - avoid unnecessary sprints");
                actions.add("Increase recovery time between efforts");
                actions.add("Monitor for progression to high fatigue");
                recoveryProtocols.add("Proper cool-down routine");
                recoveryProtocols.add("Adequate hydration");
                break;
                
            case "LOW":
                actions.add("Continue current activity level");
                actions.add("Maintain awareness of fatigue indicators");
                recoveryProtocols.add("Standard recovery protocols");
                break;
        }
        
        recommendations.put("immediateActions", actions);
        recommendations.put("recoveryProtocols", recoveryProtocols);
        recommendations.put("estimatedRecoveryTime", estimateRecoveryTime(fatigueLevel));
        recommendations.put("keyIndicators", identifyKeyFatigueIndicators(analysis));
        
        return recommendations;
    }
    
    private String getFatigueCategory(double fatigueLevel) {
        if (fatigueLevel > 0.8) return "SEVERE";
        if (fatigueLevel > 0.6) return "HIGH";
        if (fatigueLevel > 0.3) return "MODERATE";
        return "LOW";
    }
    
    private String estimateRecoveryTime(double fatigueLevel) {
        if (fatigueLevel > 0.8) return "24-48 hours";
        if (fatigueLevel > 0.6) return "12-24 hours";
        if (fatigueLevel > 0.3) return "6-12 hours";
        return "2-6 hours";
    }
    
    private List<String> identifyKeyFatigueIndicators(Map<String, Object> analysis) {
        List<String> indicators = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Double> speedDecline = (Map<String, Double>) analysis.get("speedDecline");
        if (speedDecline.get("speedDeclinePercentage") > 10) {
            indicators.add("Significant speed decline detected");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> acceleration = (Map<String, Object>) analysis.get("acceleration");
        if ((Double) acceleration.get("accelerationDecline") > 15) {
            indicators.add("Reduced acceleration capacity");
        }
        
        double movementEfficiency = (Double) analysis.get("movementEfficiency");
        if (movementEfficiency < 60) {
            indicators.add("Decreased movement efficiency");
        }
        
        @SuppressWarnings("unchecked")
        List<Double> recoveryTimes = (List<Double>) analysis.get("recoveryTimes");
        if (!recoveryTimes.isEmpty()) {
            double avgRecovery = recoveryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            if (avgRecovery > 100) {
                indicators.add("Extended recovery times between efforts");
            }
        }
        
        return indicators;
    }
    
    private double calculateFatigueConfidence(Map<String, Object> analysis) {
        double confidence = 0.7;
        
        // Increase confidence if multiple indicators align
        @SuppressWarnings("unchecked")
        Map<String, Object> heartRate = (Map<String, Object>) analysis.get("heartRate");
        if ((Boolean) heartRate.get("available")) {
            confidence += 0.15;
        }
        
        @SuppressWarnings("unchecked")
        List<Double> recoveryTimes = (List<Double>) analysis.get("recoveryTimes");
        if (recoveryTimes.size() > 3) {
            confidence += 0.1;
        }
        
        return Math.min(0.95, confidence);
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        
        return distance;
    }
    
    private MLPrediction createDefaultFatiguePrediction(String playerId) {
        MLPrediction prediction = new MLPrediction();
        prediction.setPlayerId(playerId);
        prediction.setType(MLPrediction.PredictionType.FATIGUE_LEVEL);
        
        Map<String, Object> output = new HashMap<>();
        output.put("fatigueLevel", 0.2);
        output.put("fatigueCategory", "LOW");
        output.put("immediateActions", Arrays.asList("Insufficient data for fatigue analysis"));
        output.put("recoveryProtocols", Arrays.asList("Standard recovery protocols"));
        
        prediction.setOutput(output);
        prediction.setConfidence(0.4);
        prediction.setPredictedAt(LocalDateTime.now());
        
        return prediction;
    }
}
