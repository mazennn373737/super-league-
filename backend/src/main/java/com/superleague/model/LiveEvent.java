package com.superleague.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "live_events")
public class LiveEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveEventType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "event_minute", nullable = false)
    private int minute;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public LiveEvent() {}

    public LiveEvent(Fixture fixture, LiveEventType type, Team team, Player player, int minute, String description) {
        this.fixture = fixture;
        this.type = type;
        this.team = team;
        this.player = player;
        this.minute = minute;
        this.description = description;
    }

    public UUID getId() { return id; }
    public Fixture getFixture() { return fixture; }
    public void setFixture(Fixture fixture) { this.fixture = fixture; }
    public LiveEventType getType() { return type; }
    public void setType(LiveEventType type) { this.type = type; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
