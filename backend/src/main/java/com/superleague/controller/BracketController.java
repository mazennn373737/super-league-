package com.superleague.controller;

import com.superleague.dto.BracketUpdateMessage;
import com.superleague.model.Stage;
import com.superleague.service.KnockoutBracketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bracket")
public class BracketController {

    private final KnockoutBracketService knockoutBracketService;

    public BracketController(KnockoutBracketService knockoutBracketService) {
        this.knockoutBracketService = knockoutBracketService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<BracketUpdateMessage.BracketNode>>> getFullBracket() {
        Map<String, List<BracketUpdateMessage.BracketNode>> result = new LinkedHashMap<>();
        for (Stage stage : List.of(Stage.QUARTER_FINAL, Stage.SEMI_FINAL, Stage.FINAL)) {
            List<BracketUpdateMessage.BracketNode> nodes = knockoutBracketService.getBracket(stage);
            if (!nodes.isEmpty()) {
                result.put(stage.name().toLowerCase(), nodes);
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{stage}")
    public ResponseEntity<List<BracketUpdateMessage.BracketNode>> getBracketStage(@PathVariable String stage) {
        try {
            Stage s = Stage.valueOf(stage.toUpperCase());
            return ResponseEntity.ok(knockoutBracketService.getBracket(s));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
