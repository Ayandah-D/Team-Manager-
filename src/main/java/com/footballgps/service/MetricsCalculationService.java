package com.footballgps.service;

import com.footballgps.model.GpsData;
import com.footballgps.model.PlayerMetrics;
import com.footballgps.repository.GpsDataRepository;
import com.footballgps.repository.PlayerMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsCalculationService {
    
    private final GpsDataRepository gpsDataRepository;
    private final PlayerMetricsRepository playerMetricsRepository;
    
    private static final double SPRINT_THRESHOLD = 24.0; // km/h
    private static final double HIGH_INTENSITY_THRESHOLD = 19.8; // km/h
    private static final double ACCELERATION_THRESHOLD = 3.0; // m/s²
    
    @Async
    public void calculateRealTimeMetrics(GpsData gpsData) {
        try {
            // Get recent data for context (last 5 minutes)
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            List<GpsData> recentData = gpsDataRepository.findByPlayerIdAndTimestampBetween(
                gpsData.getPlayerId(), fiveMinutesAgo, LocalDateTime.now());
            
            if (recentData.size() < 2) return; // Need at least 2 points for calculations
            
            PlayerMetrics metrics = calculateMetricsFromData(recentData, gpsData.getSessionId());
            playerMetricsRepository.save(metrics);
            
            log.debug("Real-time metrics calculated for player {}", gpsData.getPlayerId());
        } catch (Exception e) {
            log.error("Error calculating real-time metrics", e);
        }
    }
    
    public PlayerMetrics calculateSessionMetrics(String playerId, String sessionId) {
        List<GpsData> sessionData = gpsDataRepository.findByPlayerIdAndSessionId(playerId, sessionId);
        
        if (sessionData.isEmpty()) {
            return null;
        }
        
        return calculateMetricsFromData(sessionData, sessionId);
    }
    
    private PlayerMetrics calculateMetricsFromData(List<GpsData> data, String sessionId) {
        // Sort data by timestamp
        data.sort(Comparator.comparing(GpsData::getTimestamp));
        
        PlayerMetrics metrics = new PlayerMetrics();
        metrics.setPlayerId(data.get(0).getPlayerId());
        metrics.setSessionId(sessionId);
        metrics.setCalculatedAt(LocalDateTime.now());
        
        // Calculate movement metrics
        PlayerMetrics.MovementMetrics movementMetrics = calculateMovementMetrics(data);
        metrics.setMovement(movementMetrics);
        
        // Calculate performance metrics
        PlayerMetrics.PerformanceMetrics performanceMetrics = calculatePerformanceMetrics(data);
        metrics.setPerformance(performanceMetrics);
        
        // Calculate tactical metrics
        PlayerMetrics.TacticalMetrics tacticalMetrics = calculateTacticalMetrics(data);
        metrics.setTactical(tacticalMetrics);
        
        // Calculate load metrics
        PlayerMetrics.LoadMetrics loadMetrics = calculateLoadMetrics(data);
        metrics.setLoad(loadMetrics);
        
        return metrics;
    }
    
    private PlayerMetrics.MovementMetrics calculateMovementMetrics(List<GpsData> data) {
        PlayerMetrics.MovementMetrics metrics = new PlayerMetrics.MovementMetrics();
        
        double totalDistance = 0;
        double sprintDistance = 0;
        double highIntensityDistance = 0;
        int sprintCount = 0;
        int accelerationCount = 0;
        int decelerationCount = 0;
        double maxSpeed = 0;
        double totalSpeed = 0;
        int jumpCount = 0;
        double playerLoad = 0;
        
        Map<String, Double> speedZones = new HashMap<>();
        speedZones.put("walking", 0.0); // 0-7 km/h
        speedZones.put("jogging", 0.0); // 7-14 km/h
        speedZones.put("running", 0.0); // 14-19.8 km/h
        speedZones.put("high_intensity", 0.0); // 19.8-24 km/h
        speedZones.put("sprinting", 0.0); // >24 km/h
        
        for (int i = 1; i < data.size(); i++) {
            GpsData current = data.get(i);
            GpsData previous = data.get(i - 1);
            
            // Calculate distance between points
            double distance = calculateDistance(
                previous.getPosition().getLatitude(), previous.getPosition().getLongitude(),
                current.getPosition().getLatitude(), current.getPosition().getLongitude()
            );
            
            totalDistance += distance;
            double speed = current.getMovement().getSpeed();
            double acceleration = current.getMovement().getAcceleration();
            
            // Speed zone classification
            if (speed > SPRINT_THRESHOLD) {
                sprintDistance += distance;
                speedZones.put("sprinting", speedZones.get("sprinting") + distance);
                if (previous.getMovement().getSpeed() <= SPRINT_THRESHOLD) {
                    sprintCount++;
                }
            } else if (speed > HIGH_INTENSITY_THRESHOLD) {
                highIntensityDistance += distance;
                speedZones.put("high_intensity", speedZones.get("high_intensity") + distance);
            } else if (speed > 14.0) {
                speedZones.put("running", speedZones.get("running") + distance);
            } else if (speed > 7.0) {
                speedZones.put("jogging", speedZones.get("jogging") + distance);
            } else {
                speedZones.put("walking", speedZones.get("walking") + distance);
            }
            
            // Acceleration/deceleration events
            if (acceleration > ACCELERATION_THRESHOLD) {
                accelerationCount++;
            } else if (acceleration < -ACCELERATION_THRESHOLD) {
                decelerationCount++;
            }
            
            // Max speed tracking
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
            
            totalSpeed += speed;
            
            // Jump detection (simplified - based on vertical acceleration)
            if (current.getMovement().getImu() != null && 
                current.getMovement().getImu().getAccelerometer().getZ() > 15.0) {
                jumpCount++;
            }
            
            // Player load calculation (simplified)
            if (current.getMovement().getImu() != null) {
                double vectorMagnitude = Math.sqrt(
                    Math.pow(current.getMovement().getImu().getAccelerometer().getX(), 2) +
                    Math.pow(current.getMovement().getImu().getAccelerometer().getY(), 2) +
                    Math.pow(current.getMovement().getImu().getAccelerometer().getZ(), 2)
                );
                playerLoad += vectorMagnitude / 100.0; // Normalize
            }
        }
        
        metrics.setTotalDistance(totalDistance);
        metrics.setSprintDistance(sprintDistance);
        metrics.setHighIntensityDistance(highIntensityDistance);
        metrics.setSprintCount(sprintCount);
        metrics.setAccelerationCount(accelerationCount);
        metrics.setDecelerationCount(decelerationCount);
        metrics.setMaxSpeed(maxSpeed);
        metrics.setAverageSpeed(data.size() > 1 ? totalSpeed / (data.size() - 1) : 0);
        metrics.setJumpCount(jumpCount);
        metrics.setPlayerLoad(playerLoad);
        metrics.setSpeedZones(speedZones);
        
        return metrics;
    }
    
    private PlayerMetrics.PerformanceMetrics calculatePerformanceMetrics(List<GpsData> data) {
        PlayerMetrics.PerformanceMetrics metrics = new PlayerMetrics.PerformanceMetrics();
        
        int totalHeartRate = 0;
        int maxHeartRate = 0;
        int validHeartRateReadings = 0;
        double totalIntensity = 0;
        
        for (GpsData point : data) {
            if (point.getBiometrics() != null && point.getBiometrics().getHeartRate() > 0) {
                int hr = point.getBiometrics().getHeartRate();
                totalHeartRate += hr;
                validHeartRateReadings++;
                
                if (hr > maxHeartRate) {
                    maxHeartRate = hr;
                }
            }
            
            // Calculate intensity based on speed and acceleration
            double speed = point.getMovement().getSpeed();
            double acceleration = Math.abs(point.getMovement().getAcceleration());
            double intensity = (speed / 30.0) + (acceleration / 5.0); // Normalized intensity
            totalIntensity += Math.min(intensity, 10.0); // Cap at 10
        }
        
        metrics.setMaxHeartRate(maxHeartRate);
        metrics.setAverageHeartRate(validHeartRateReadings > 0 ? totalHeartRate / validHeartRateReadings : 0);
        metrics.setIntensityScore(data.size() > 0 ? totalIntensity / data.size() : 0);
        
        // Simplified calculations for other metrics
        metrics.setWorkRate(85.0); // Would be calculated based on expected vs actual performance
        metrics.setFatigueIndex(5.0); // Would be calculated based on performance decline
        metrics.setRecoveryTime(120.0); // Would be calculated based on heart rate recovery
        metrics.setVo2Max(45.0); // Would be estimated from performance data
        
        return metrics;
    }
    
    private PlayerMetrics.TacticalMetrics calculateTacticalMetrics(List<GpsData> data) {
        PlayerMetrics.TacticalMetrics metrics = new PlayerMetrics.TacticalMetrics();
        
        // Calculate average position
        double totalX = 0, totalY = 0;
        Map<String, Double> heatMap = new HashMap<>();
        
        for (GpsData point : data) {
            // Convert GPS coordinates to field coordinates (simplified)
            double fieldX = convertToFieldX(point.getPosition().getLongitude());
            double fieldY = convertToFieldY(point.getPosition().getLatitude());
            
            totalX += fieldX;
            totalY += fieldY;
            
            // Create heat map zones (simplified grid)
            String zone = getFieldZone(fieldX, fieldY);
            heatMap.put(zone, heatMap.getOrDefault(zone, 0.0) + 1.0);
        }
        
        metrics.setAveragePositionX(data.size() > 0 ? totalX / data.size() : 0);
        metrics.setAveragePositionY(data.size() > 0 ? totalY / data.size() : 0);
        metrics.setHeatMap(heatMap);
        
        // Simplified calculations for other tactical metrics
        metrics.setFieldCoverage(75.0); // Would be calculated based on area covered
        metrics.setFormationAdherence(80.0); // Would be calculated based on expected position
        metrics.setTeamSynchronization(70.0); // Would be calculated based on team movement
        metrics.setPassingNetworkConnections(5); // Would be calculated from game events
        
        return metrics;
    }
    
    private PlayerMetrics.LoadMetrics calculateLoadMetrics(List<GpsData> data) {
        PlayerMetrics.LoadMetrics metrics = new PlayerMetrics.LoadMetrics();
        
        // Simplified load calculations
        double sessionLoad = data.size() * 0.1; // Based on data points and intensity
        
        metrics.setAcuteLoad(sessionLoad); // Would be 7-day rolling average
        metrics.setChronicLoad(sessionLoad * 0.8); // Would be 28-day rolling average
        metrics.setAcuteChronicRatio(metrics.getAcuteLoad() / metrics.getChronicLoad());
        metrics.setTrainingStressScore(sessionLoad * 10);
        metrics.setRecoveryHours(24); // Would be calculated based on load and individual factors
        metrics.setReadinessScore(8.0); // Would be calculated based on various factors
        
        return metrics;
    }
    
    // Utility methods
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
    
    private double convertToFieldX(double longitude) {
        // Simplified conversion - would need actual field calibration
        return longitude * 100000; // Convert to field coordinates
    }
    
    private double convertToFieldY(double latitude) {
        // Simplified conversion - would need actual field calibration
        return latitude * 100000; // Convert to field coordinates
    }
    
    private String getFieldZone(double x, double y) {
        // Simplified zone calculation - divide field into 9 zones
        int zoneX = (int) (x / 33.33) + 1; // 3 zones horizontally
        int zoneY = (int) (y / 33.33) + 1; // 3 zones vertically
        return "zone_" + Math.min(zoneX, 3) + "_" + Math.min(zoneY, 3);
    }
}
