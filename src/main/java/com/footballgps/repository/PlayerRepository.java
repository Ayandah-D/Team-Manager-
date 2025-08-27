package com.footballgps.repository;

import com.footballgps.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    List<Player> findByTeamId(String teamId);
    List<Player> findByPosition(String position);
    Optional<Player> findByDeviceId(String deviceId);
    List<Player> findByActiveTrue();
    
    @Query("{'teamId': ?0, 'active': true}")
    List<Player> findActivePlayersByTeam(String teamId);
}
