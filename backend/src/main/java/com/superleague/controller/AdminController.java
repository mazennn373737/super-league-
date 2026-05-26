package com.superleague.controller;

import com.superleague.dto.FixtureUpdateMessage;
import com.superleague.model.*;
import com.superleague.service.KnockoutBracketService;
import com.superleague.service.LiveEventService;
import com.superleague.service.StandingService;
import com.superleague.websocket.LiveScoreWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LiveEventService liveEventService;
    private final StandingService standingService;
    private final KnockoutBracketService knockoutBracketService;
    private final LiveScoreWebSocketHandler webSocketHandler;

    public AdminController(LiveEventService liveEventService, StandingService standingService,
                          KnockoutBracketService knockoutBracketService, LiveScoreWebSocketHandler webSocketHandler) {
        this.liveEventService = liveEventService;
        this.standingService = standingService;
        this.knockoutBracketService = knockoutBracketService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/events/goal")
    public ResponseEntity<FixtureUpdateMessage> injectGoal(@RequestBody Map<String, Object> payload) {
        UUID fixtureId = UUID.fromString(payload.get("fixtureId").toString());
        UUID teamId = UUID.fromString(payload.get("teamId").toString());
        int minute = Integer.parseInt(payload.get("minute").toString());
        String description = payload.getOrDefault("description", "Goal!").toString();
        return ResponseEntity.ok(liveEventService.processGoalEvent(fixtureId, teamId, null, minute, description));
    }

    @PostMapping("/events/status")
    public ResponseEntity<FixtureUpdateMessage> updateStatus(@RequestBody Map<String, Object> payload) {
        UUID fixtureId = UUID.fromString(payload.get("fixtureId").toString());
        MatchStatus status = MatchStatus.valueOf(payload.get("status").toString().toUpperCase());
        return ResponseEntity.ok(liveEventService.updateMatchStatus(fixtureId, status));
    }

    @PostMapping("/standings/recalculate")
    public ResponseEntity<String> recalculateStandings(@RequestBody Map<String, Object> payload) {
        Division division = Division.valueOf(payload.get("division").toString().toUpperCase());
        String groupName = (String) payload.getOrDefault("groupName", "A");
        standingService.recalculateStandings(division, groupName);
        return ResponseEntity.ok("Standings recalculated for " + division + " group " + groupName);
    }

    @PostMapping("/bracket/initialize")
    public ResponseEntity<String> initializeBracket(@RequestBody Map<String, Object> payload) {
        Division division = Division.valueOf(payload.get("division").toString().toUpperCase());
        knockoutBracketService.initializeBracket(division);
        return ResponseEntity.ok("Bracket initialized for " + division);
    }

    @GetMapping("/ws/connections")
    public ResponseEntity<Map<String, Integer>> getConnectionCount() {
        return ResponseEntity.ok(Map.of("activeConnections", webSocketHandler.getActiveConnectionCount()));
    }
}
