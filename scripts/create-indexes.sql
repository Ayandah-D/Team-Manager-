-- Create indexes for optimal query performance

-- GPS Data indexes
db.gps_data.createIndex({ "playerId": 1, "sessionId": 1 })
db.gps_data.createIndex({ "sessionId": 1, "timestamp": 1 })
db.gps_data.createIndex({ "playerId": 1, "timestamp": 1 })
db.gps_data.createIndex({ "timestamp": 1 })

-- Player indexes
db.players.createIndex({ "teamId": 1, "active": 1 })
db.players.createIndex({ "deviceId": 1 }, { unique: true })
db.players.createIndex({ "active": 1 })

-- Player Metrics indexes
db.player_metrics.createIndex({ "playerId": 1, "calculatedAt": 1 })
db.player_metrics.createIndex({ "sessionId": 1 })
db.player_metrics.createIndex({ "playerId": 1, "sessionId": 1 }, { unique: true })

-- Training Session indexes
db.training_sessions.createIndex({ "teamId": 1, "startTime": 1 })
db.training_sessions.createIndex({ "active": 1 })
db.training_sessions.createIndex({ "startTime": 1, "endTime": 1 })

-- Compound indexes for complex queries
db.gps_data.createIndex({ "playerId": 1, "sessionId": 1, "timestamp": 1 })
db.player_metrics.createIndex({ "playerId": 1, "calculatedAt": -1 })
