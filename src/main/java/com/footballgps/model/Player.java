package com.footballgps.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "players")
public class Player {
    @Id
    private String id;
    private String name;
    private String position;
    private int jerseyNumber;
    private String teamId;
    private LocalDate dateOfBirth;
    private double height; // in cm
    private double weight; // in kg
    private String deviceId;
    private boolean active;
    private PlayerProfile profile;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerProfile {
        private double maxSpeed; // km/h
        private double averageSpeed; // km/h
        private int maxHeartRate;
        private int restingHeartRate;
        private List<String> preferredPositions;
        private double fitnessLevel; // 1-10 scale
    }
}
