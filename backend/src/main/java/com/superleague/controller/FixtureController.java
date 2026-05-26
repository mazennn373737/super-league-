package com.superleague.controller;

import com.superleague.dto.FixtureUpdateMessage;
import com.superleague.model.*;
import com.superleague.repository.FixtureRepository;
import com.superleague.service.LiveEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fixtures")
public class FixtureController {

    private final FixtureRepository fixtureRepository;
    private final LiveEventService liveEventService;

    public FixtureController(FixtureRepository fixtureRepository, LiveEventService liveEventService) {
        this.fixtureRepository = fixtureRepository;
        this.liveEventService = liveEventService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFixtures() {
        List<Fixture> fixtures = fixtureRepository.findAll();
        List<Map<String, Object>> response = fixtures.stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("homeTeam", f.getHomeTeam().getName());
            m.put("awayTeam", f.getAwayTeam().getName());
            m.put("homeTeamShort", f.getHomeTeam().getShortName());
            m.put("awayTeamShort", f.getAwayTeam().getShortName());
            m.put("homeScore", f.getHomeScore());
            m.put("awayScore", f.getAwayScore());
            m.put("status", f.getStatus());
            m.put("minute", f.getMinute());
            m.put("division", f.getDivision());
            m.put("stage", f.getStage());
            m.put("groupName", f.getGroupName());
            return m;
        }).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    public ResponseEntity<List<Map<String, Object>>> getLiveFixtures() {
        List<Fixture> live = fixtureRepository.findByStatus(MatchStatus.FIRST_HALF);
        live.addAll(fixtureRepository.findByStatus(MatchStatus.SECOND_HALF));
        live.addAll(fixtureRepository.findByStatus(MatchStatus.HALFTIME));

        List<Map<String, Object>> response = live.stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("homeTeam", f.getHomeTeam().getName());
            m.put("awayTeam", f.getAwayTeam().getName());
            m.put("homeTeamShort", f.getHomeTeam().getShortName());
            m.put("awayTeamShort", f.getAwayTeam().getShortName());
            m.put("homeScore", f.getHomeScore());
            m.put("awayScore", f.getAwayScore());
            m.put("status", f.getStatus());
            m.put("minute", f.getMinute());
            m.put("division", f.getDivision());
            return m;
        }).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixtureUpdateMessage> getFixture(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(liveEventService.getFixtureUpdate(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
