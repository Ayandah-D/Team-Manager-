package com.footballgps.service;

import com.footballgps.model.GpsData;
import com.footballgps.model.PlayerMetrics;
import com.footballgps.repository.GpsDataRepository;
import com.footballgps.repository.PlayerMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsCalculationServiceTest {

    @Mock
    private GpsDataRepository gpsDataRepository;

    @Mock
    private PlayerMetricsRepository playerMetricsRepository;

    @InjectMocks
    private MetricsCalculationService metricsCalculationService;

    private List<GpsData> sampleGpsData;

    @BeforeEach
    void setUp() {
        sampleGpsData = createSampleGpsData();
    }

    @Test
    void testCalculateSessionMetrics() {
        // Given
        String playerId = "player_001";
        String sessionId = "session_001";
        
        when(gpsDataRepository.findByPlayerIdAndSessionId(playerId, sessionId))
                .thenReturn(sampleGpsData);

        // When
        PlayerMetrics result = metricsCalculationService.calculateSessionMetrics(playerId, sessionId);

        // Then
        assertNotNull(result);
        assertEquals(playerId, result.getPlayerId());
        assertEquals(sessionId, result.getSessionId());
        assertNotNull(result.getMovement());
        assertNotNull(result.getPerformance());
        assertNotNull(result.getTactical());
        assertNotNull(result.getLoad());
        
        // Verify movement metrics
        assertTrue(result.getMovement().getTotalDistance() > 0);
        assertTrue(result.getMovement().getMaxSpeed() > 0);
        assertNotNull(result.getMovement().getSpeedZones());
    }

    @Test
    void testCalculateRealTimeMetrics() {
        // Given
        GpsData gpsData = sampleGpsData.get(0);
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        when(gpsDataRepository.findByPlayerIdAndTimestampBetween(
                eq(gpsData.getPlayerId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(sampleGpsData);
        when(playerMetricsRepository.save(any(PlayerMetrics.class)))
                .thenReturn(new PlayerMetrics());

        // When
        metricsCalculationService.calculateRealTimeMetrics(gpsData);

        // Then
        verify(playerMetricsRepository, times(1)).save(any(PlayerMetrics.class));
    }

    private List<GpsData> createSampleGpsData() {
        GpsData data1 = new GpsData();
        data1.setPlayerId("player_001");
        data1.setSessionId("session_001");
        data1.setTimestamp(LocalDateTime.now().minusMinutes(10));
        
        GpsData.Position position1 = new GpsData.Position();
        position1.setLatitude(53.4631);
        position1.setLongitude(-2.2914);
        position1.setAltitude(50.0);
        position1.setAccuracy(2.0);
        position1.setSatellites(12);
        data1.setPosition(position1);
        
        GpsData.Movement movement1 = new GpsData.Movement();
        movement1.setSpeed(15.5);
        movement1.setAcceleration(2.1);
        movement1.setDirection(45.0);
        
        GpsData.Movement.ImuData imu1 = new GpsData.Movement.ImuData();
        imu1.setAccelerometer(new GpsData.Vector3D(0.5, 0.3, 9.8));
        imu1.setGyroscope(new GpsData.Vector3D(0.1, 0.2, 0.1));
        imu1.setMagnetometer(new GpsData.Vector3D(25.0, 30.0, 45.0));
        movement1.setImu(imu1);
        data1.setMovement(movement1);
        
        GpsData.Biometrics bio1 = new GpsData.Biometrics();
        bio1.setHeartRate(150);
        bio1.setBodyTemperature(37.2);
        bio1.setStressLevel(6);
        data1.setBiometrics(bio1);

        // Create second data point
        GpsData data2 = new GpsData();
        data2.setPlayerId("player_001");
        data2.setSessionId("session_001");
        data2.setTimestamp(LocalDateTime.now().minusMinutes(9));
        
        GpsData.Position position2 = new GpsData.Position();
        position2.setLatitude(53.4635);
        position2.setLongitude(-2.2918);
        position2.setAltitude(51.0);
        position2.setAccuracy(1.8);
        position2.setSatellites(13);
        data2.setPosition(position2);
        
        GpsData.Movement movement2 = new GpsData.Movement();
        movement2.setSpeed(25.2);
        movement2.setAcceleration(3.5);
        movement2.setDirection(50.0);
        
        GpsData.Movement.ImuData imu2 = new GpsData.Movement.ImuData();
        imu2.setAccelerometer(new GpsData.Vector3D(1.2, 0.8, 10.2));
        imu2.setGyroscope(new GpsData.Vector3D(0.3, 0.4, 0.2));
        imu2.setMagnetometer(new GpsData.Vector3D(26.0, 31.0, 46.0));
        movement2.setImu(imu2);
        data2.setMovement(movement2);
        
        GpsData.Biometrics bio2 = new GpsData.Biometrics();
        bio2.setHeartRate(165);
        bio2.setBodyTemperature(37.5);
        bio2.setStressLevel(7);
        data2.setBiometrics(bio2);

        return Arrays.asList(data1, data2);
    }
}
