package com.footballgps.controller;

import com.footballgps.model.Player;
import com.footballgps.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PlayerController {
    
    private final PlayerService playerService;
    
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        log.info("Fetching all players");
        List<Player> players = playerService.getAllPlayers();
        log.info("Found {} players", players.size());
        return ResponseEntity.ok(players);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable String id) {
        log.info("Fetching player with id: {}", id);
        return playerService.getPlayerById(id)
                .map(player -> {
                    log.info("Found player: {}", player.getName());
                    return ResponseEntity.ok(player);
                })
                .orElseGet(() -> {
                    log.warn("Player not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }
    
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Player>> getPlayersByTeam(@PathVariable String teamId) {
        log.info("Fetching players for team: {}", teamId);
        List<Player> players = playerService.getPlayersByTeam(teamId);
        log.info("Found {} players for team {}", players.size(), teamId);
        return ResponseEntity.ok(players);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Player>> getActivePlayers() {
        log.info("Fetching active players");
        try {
            List<Player> players = playerService.getActivePlayers();
            log.info("Found {} active players", players.size());
            return ResponseEntity.ok(players);
        } catch (Exception e) {
            log.error("Error fetching active players", e);
            throw e;
        }
    }
    
    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        log.info("Creating new player: {}", player.getName());
        try {
            Player created = playerService.createPlayer(player);
            log.info("Successfully created player with id: {}", created.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating player", e);
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String id, @RequestBody Player player) {
        log.info("Updating player with id: {}", id);
        player.setId(id);
        Player updated = playerService.updatePlayer(player);
        log.info("Successfully updated player: {}", updated.getName());
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable String id) {
        log.info("Deleting player with id: {}", id);
        playerService.deletePlayer(id);
        log.info("Successfully deleted player with id: {}", id);
        return ResponseEntity.ok().build();
    }
}
