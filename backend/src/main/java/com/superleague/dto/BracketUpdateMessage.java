package com.superleague.dto;

import com.superleague.model.Stage;
import java.util.List;
import java.util.UUID;

public class BracketUpdateMessage {
    private Stage stage;
    private List<BracketNode> nodes;

    public BracketUpdateMessage() {}

    public BracketUpdateMessage(Stage stage, List<BracketNode> nodes) {
        this.stage = stage;
        this.nodes = nodes;
    }

    public Stage getStage() { return stage; }
    public List<BracketNode> getNodes() { return nodes; }

    public static class BracketNode {
        private UUID bracketId;
        private UUID fixtureId;
        private String homeTeam;
        private String awayTeam;
        private String homeTeamShort;
        private String awayTeamShort;
        private int homeScore;
        private int awayScore;
        private int position;
        private UUID parentWinnerId;
        private UUID parentLoserId;
        private String status;

        public BracketNode() {}

        public BracketNode(UUID bracketId, UUID fixtureId, String homeTeam, String awayTeam,
                          String homeTeamShort, String awayTeamShort, int homeScore, int awayScore,
                          int position, UUID parentWinnerId, UUID parentLoserId, String status) {
            this.bracketId = bracketId; this.fixtureId = fixtureId; this.homeTeam = homeTeam;
            this.awayTeam = awayTeam; this.homeTeamShort = homeTeamShort; this.awayTeamShort = awayTeamShort;
            this.homeScore = homeScore; this.awayScore = awayScore; this.position = position;
            this.parentWinnerId = parentWinnerId; this.parentLoserId = parentLoserId; this.status = status;
        }

        public UUID getBracketId() { return bracketId; }
        public UUID getFixtureId() { return fixtureId; }
        public String getHomeTeam() { return homeTeam; }
        public String getAwayTeam() { return awayTeam; }
        public String getHomeTeamShort() { return homeTeamShort; }
        public String getAwayTeamShort() { return awayTeamShort; }
        public int getHomeScore() { return homeScore; }
        public int getAwayScore() { return awayScore; }
        public int getPosition() { return position; }
        public UUID getParentWinnerId() { return parentWinnerId; }
        public UUID getParentLoserId() { return parentLoserId; }
        public String getStatus() { return status; }
    }
}
