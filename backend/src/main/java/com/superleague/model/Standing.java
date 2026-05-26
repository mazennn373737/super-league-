package com.superleague.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "standings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "division", "group_name"})
})
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Division division;

    @Column(name = "group_name")
    private String groupName;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private int played;

    @Column(nullable = false)
    private int won;

    @Column(nullable = false)
    private int drawn;

    @Column(nullable = false)
    private int lost;

    @Column(name = "goals_for", nullable = false)
    private int goalsFor;

    @Column(name = "goals_against", nullable = false)
    private int goalsAgainst;

    @Column(name = "goal_difference", nullable = false)
    private int goalDifference;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Standing() {}

    public Standing(Team team, Division division, String groupName) {
        this.team = team;
        this.division = division;
        this.groupName = groupName;
    }

    public UUID getId() { return id; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public Division getDivision() { return division; }
    public void setDivision(Division division) { this.division = division; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }
    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }
    public int getDrawn() { return drawn; }
    public void setDrawn(int drawn) { this.drawn = drawn; }
    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }
    public int getGoalsFor() { return goalsFor; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }
    public int getGoalDifference() { return goalDifference; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
