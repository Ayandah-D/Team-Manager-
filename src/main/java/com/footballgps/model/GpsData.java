package com.footballgps.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gps_data")
public class GpsData {
    @Id
    private String id;
    private String playerId;
    private String sessionId;
    private LocalDateTime timestamp;
    private Position position;
    private Movement movement;
    private Biometrics biometrics;
    private Environmental environmental;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private double latitude;
        private double longitude;
        private double altitude;
        private double accuracy; // in meters
        private int satellites;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Movement {
        private double speed; // km/h
        private double acceleration; // m/s²
        private double direction; // degrees
        private ImuData imu;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ImuData {
            private Vector3D accelerometer; // m/s²
            private Vector3D gyroscope; // rad/s
            private Vector3D magnetometer; // µT
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Biometrics {
        private int heartRate; // bpm
        private double bodyTemperature; // °C
        private int stressLevel; // 1-10 scale
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Environmental {
        private double temperature; // °C
        private double pressure; // hPa
        private double humidity; // %
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Vector3D {
        private double x;
        private double y;
        private double z;
    }
}
