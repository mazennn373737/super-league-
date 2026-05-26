package com.superleague.dto;

import com.superleague.model.Division;
import java.util.List;
import java.util.UUID;

public class StandingUpdateMessage {
    private Division division;
    private List<StandingRow> standings;

    public StandingUpdateMessage() {}

    public StandingUpdateMessage(Division division, List<StandingRow> standings) {
        this.division = division;
        this.standings = standings;
    }

    public Division getDivision() { return division; }
    public List<StandingRow> getStandings() { return standings; }

    public static class StandingRow {
        private UUID teamId;
        private String teamName;
        private String teamShort;
        private int position;
        private int points;
        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;
        private int goalDifference;

        public StandingRow() {}

        public StandingRow(UUID teamId, String teamName, String teamShort, int position, int points,
                          int played, int won, int drawn, int lost, int goalsFor, int goalsAgainst, int goalDifference) {
            this.teamId = teamId; this.teamName = teamName; this.teamShort = teamShort;
            this.position = position; this.points = points; this.played = played;
            this.won = won; this.drawn = drawn; this.lost = lost;
            this.goalsFor = goalsFor; this.goalsAgainst = goalsAgainst; this.goalDifference = goalDifference;
        }

        public UUID getTeamId() { return teamId; }
        public String getTeamName() { return teamName; }
        public String getTeamShort() { return teamShort; }
        public int getPosition() { return position; }
        public int getPoints() { return points; }
        public int getPlayed() { return played; }
        public int getWon() { return won; }
        public int getDrawn() { return drawn; }
        public int getLost() { return lost; }
        public int getGoalsFor() { return goalsFor; }
        public int getGoalsAgainst() { return goalsAgainst; }
        public int getGoalDifference() { return goalDifference; }
    }
}
