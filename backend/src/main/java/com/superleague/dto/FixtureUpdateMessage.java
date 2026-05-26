package com.superleague.dto;

import com.superleague.model.*;

import java.util.List;
import java.util.UUID;

public class FixtureUpdateMessage {
    private UUID fixtureId;
    private String homeTeam;
    private String awayTeam;
    private String homeTeamShort;
    private String awayTeamShort;
    private int homeScore;
    private int awayScore;
    private MatchStatus status;
    private int minute;
    private Division division;
    private Stage stage;
    private List<LiveEventDTO> events;

    public FixtureUpdateMessage() {}

    public FixtureUpdateMessage(Fixture fixture, List<LiveEvent> events) {
        this.fixtureId = fixture.getId();
        this.homeTeam = fixture.getHomeTeam().getName();
        this.awayTeam = fixture.getAwayTeam().getName();
        this.homeTeamShort = fixture.getHomeTeam().getShortName();
        this.awayTeamShort = fixture.getAwayTeam().getShortName();
        this.homeScore = fixture.getHomeScore();
        this.awayScore = fixture.getAwayScore();
        this.status = fixture.getStatus();
        this.minute = fixture.getMinute();
        this.division = fixture.getDivision();
        this.stage = fixture.getStage();
        this.events = events.stream().map(e -> new LiveEventDTO(
            e.getType(), e.getTeam().getName(), e.getPlayer() != null ? e.getPlayer().getName() : null,
            e.getMinute(), e.getDescription()
        )).toList();
    }

    public UUID getFixtureId() { return fixtureId; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public String getHomeTeamShort() { return homeTeamShort; }
    public String getAwayTeamShort() { return awayTeamShort; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public MatchStatus getStatus() { return status; }
    public int getMinute() { return minute; }
    public Division getDivision() { return division; }
    public Stage getStage() { return stage; }
    public List<LiveEventDTO> getEvents() { return events; }

    public static class LiveEventDTO {
        private LiveEventType type;
        private String team;
        private String player;
        private int minute;
        private String description;

        public LiveEventDTO() {}
        public LiveEventDTO(LiveEventType type, String team, String player, int minute, String description) {
            this.type = type; this.team = team; this.player = player; this.minute = minute; this.description = description;
        }
        public LiveEventType getType() { return type; }
        public String getTeam() { return team; }
        public String getPlayer() { return player; }
        public int getMinute() { return minute; }
        public String getDescription() { return description; }
    }
}
