package com.superleague.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fixtures")
public class Fixture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Division division;

    @Column(name = "group_name")
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(name = "home_score")
    private int homeScore;

    @Column(name = "away_score")
    private int awayScore;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "match_minute", nullable = false)
    private int minute;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = MatchStatus.NOT_STARTED;
        minute = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Fixture() {}

    public Fixture(Team homeTeam, Team awayTeam, Division division, String groupName, Stage stage, LocalDateTime startTime) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.division = division;
        this.groupName = groupName;
        this.stage = stage;
        this.startTime = startTime;
        this.status = MatchStatus.NOT_STARTED;
        this.minute = 0;
    }

    public UUID getId() { return id; }
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    public Division getDivision() { return division; }
    public void setDivision(Division division) { this.division = division; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }
    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
