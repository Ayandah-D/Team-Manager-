-- Insert sample teams
db.teams.insertMany([
  {
    "_id": "team_001",
    "name": "Manchester United",
    "league": "Premier League",
    "founded": 1878,
    "stadium": "Old Trafford"
  },
  {
    "_id": "team_002", 
    "name": "Barcelona FC",
    "league": "La Liga",
    "founded": 1899,
    "stadium": "Camp Nou"
  }
])

-- Insert sample players
db.players.insertMany([
  {
    "_id": "player_001",
    "name": "Marcus Rashford",
    "position": "Forward",
    "jerseyNumber": 10,
    "teamId": "team_001",
    "dateOfBirth": ISODate("1997-10-31"),
    "height": 180,
    "weight": 70,
    "deviceId": "GPS_001",
    "active": true,
    "profile": {
      "maxSpeed": 35.2,
      "averageSpeed": 12.5,
      "maxHeartRate": 195,
      "restingHeartRate": 45,
      "preferredPositions": ["LW", "ST", "RW"],
      "fitnessLevel": 9.2
    }
  },
  {
    "_id": "player_002",
    "name": "Bruno Fernandes",
    "position": "Midfielder",
    "jerseyNumber": 18,
    "teamId": "team_001",
    "dateOfBirth": ISODate("1994-09-08"),
    "height": 179,
    "weight": 69,
    "deviceId": "GPS_002",
    "active": true,
    "profile": {
      "maxSpeed": 32.8,
      "averageSpeed": 11.8,
      "maxHeartRate": 190,
      "restingHeartRate": 48,
      "preferredPositions": ["CAM", "CM"],
      "fitnessLevel": 8.8
    }
  },
  {
    "_id": "player_003",
    "name": "Harry Maguire",
    "position": "Defender",
    "jerseyNumber": 5,
    "teamId": "team_001",
    "dateOfBirth": ISODate("1993-03-05"),
    "height": 194,
    "weight": 100,
    "deviceId": "GPS_003",
    "active": true,
    "profile": {
      "maxSpeed": 28.5,
      "averageSpeed": 9.2,
      "maxHeartRate": 185,
      "restingHeartRate": 52,
      "preferredPositions": ["CB"],
      "fitnessLevel": 7.5
    }
  },
  {
    "_id": "player_004",
    "name": "Lionel Messi",
    "position": "Forward",
    "jerseyNumber": 10,
    "teamId": "team_002",
    "dateOfBirth": ISODate("1987-06-24"),
    "height": 170,
    "weight": 67,
    "deviceId": "GPS_004",
    "active": true,
    "profile": {
      "maxSpeed": 32.5,
      "averageSpeed": 13.1,
      "maxHeartRate": 180,
      "restingHeartRate": 40,
      "preferredPositions": ["RW", "CAM", "ST"],
      "fitnessLevel": 9.8
    }
  }
])

-- Insert sample training session
db.training_sessions.insertOne({
  "_id": "session_001",
  "teamId": "team_001",
  "name": "Pre-Season Training",
  "type": "TRAINING",
  "startTime": ISODate("2024-01-15T10:00:00Z"),
  "endTime": ISODate("2024-01-15T12:00:00Z"),
  "playerIds": ["player_001", "player_002", "player_003"],
  "coachId": "coach_001",
  "metrics": {
    "averageIntensity": 7.5,
    "totalDistance": 8500,
    "totalSprints": 45,
    "averageHeartRate": 155,
    "injuries": 0
  },
  "active": false
})
