package com.superleague.controller;

import com.superleague.dto.StandingUpdateMessage;
import com.superleague.model.Division;
import com.superleague.service.StandingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/standings")
public class StandingController {

    private final StandingService standingService;

    public StandingController(StandingService standingService) {
        this.standingService = standingService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<StandingUpdateMessage.StandingRow>>> getAllStandings() {
        Map<String, List<StandingUpdateMessage.StandingRow>> result = new LinkedHashMap<>();
        for (Division division : Division.values()) {
            result.put(division.name().toLowerCase(), standingService.getStandingsForDivision(division));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{division}")
    public ResponseEntity<List<StandingUpdateMessage.StandingRow>> getDivisionStandings(@PathVariable String division) {
        try {
            Division div = Division.valueOf(division.toUpperCase());
            return ResponseEntity.ok(standingService.getStandingsForDivision(div));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
