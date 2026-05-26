package com.superleague.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "knockout_brackets")
public class KnockoutBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_winner_id")
    private KnockoutBracket parentWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_loser_id")
    private KnockoutBracket parentLoser;

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

    public KnockoutBracket() {}

    public KnockoutBracket(Fixture fixture, Stage stage, int position) {
        this.fixture = fixture;
        this.stage = stage;
        this.position = position;
    }

    public UUID getId() { return id; }
    public Fixture getFixture() { return fixture; }
    public void setFixture(Fixture fixture) { this.fixture = fixture; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public KnockoutBracket getParentWinner() { return parentWinner; }
    public void setParentWinner(KnockoutBracket parentWinner) { this.parentWinner = parentWinner; }
    public KnockoutBracket getParentLoser() { return parentLoser; }
    public void setParentLoser(KnockoutBracket parentLoser) { this.parentLoser = parentLoser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
