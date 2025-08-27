package com.footballgps.service;

import com.footballgps.model.Player;
import com.footballgps.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    public Optional<Player> getPlayerById(String id) {
        return playerRepository.findById(id);
    }
    
    public List<Player> getPlayersByTeam(String teamId) {
        return playerRepository.findActivePlayersByTeam(teamId);
    }
    
    public List<Player> getActivePlayers() {
        return playerRepository.findByActiveTrue();
    }
    
    public Player createPlayer(Player player) {
        player.setActive(true);
        Player saved = playerRepository.save(player);
        log.info("Created new player: {} (ID: {})", saved.getName(), saved.getId());
        return saved;
    }
    
    public Player updatePlayer(Player player) {
        Player updated = playerRepository.save(player);
        log.info("Updated player: {} (ID: {})", updated.getName(), updated.getId());
        return updated;
    }
    
    public void deletePlayer(String id) {
        playerRepository.deleteById(id);
        log.info("Deleted player with ID: {}", id);
    }
    
    public Optional<Player> getPlayerByDeviceId(String deviceId) {
        return playerRepository.findByDeviceId(deviceId);
    }
}
