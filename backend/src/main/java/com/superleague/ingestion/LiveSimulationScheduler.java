package com.superleague.ingestion;

import com.superleague.model.*;
import com.superleague.repository.FixtureRepository;
import com.superleague.service.LiveEventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class LiveSimulationScheduler {

    private final FixtureRepository fixtureRepository;
    private final LiveEventService liveEventService;
    private final Random random = new Random();

    public LiveSimulationScheduler(FixtureRepository fixtureRepository, LiveEventService liveEventService) {
        this.fixtureRepository = fixtureRepository;
        this.liveEventService = liveEventService;
    }

    @Scheduled(fixedRate = 8000)
    @Transactional
    public void simulateLiveMatchEvents() {
        List<Fixture> liveFixtures = fixtureRepository.findByStatus(MatchStatus.FIRST_HALF);
        liveFixtures.addAll(fixtureRepository.findByStatus(MatchStatus.SECOND_HALF));

        if (liveFixtures.isEmpty()) {
            startNewMatch();
            return;
        }

        for (Fixture fixture : liveFixtures) {
            int newMinute = fixture.getMinute() + random.nextInt(3) + 1;

            if (fixture.getStatus() == MatchStatus.FIRST_HALF && newMinute >= 45) {
                liveEventService.updateMatchStatus(fixture.getId(), MatchStatus.HALFTIME);
                liveEventService.updateMatchMinute(fixture.getId(), 45);
                continue;
            }

            if (fixture.getStatus() == MatchStatus.SECOND_HALF && newMinute >= 90) {
                liveEventService.updateMatchStatus(fixture.getId(), MatchStatus.FINISHED);
                liveEventService.updateMatchMinute(fixture.getId(), 90);
                continue;
            }

            liveEventService.updateMatchMinute(fixture.getId(), Math.min(newMinute, 90));

            if (random.nextInt(100) < 25) {
                simulateGoal(fixture);
            }

            if (random.nextInt(100) < 5) {
                simulateCard(fixture);
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void startNewMatch() {
        List<Fixture> notStarted = fixtureRepository.findByStatus(MatchStatus.NOT_STARTED);
        if (!notStarted.isEmpty()) {
            Fixture fixture = notStarted.get(random.nextInt(notStarted.size()));
            liveEventService.updateMatchStatus(fixture.getId(), MatchStatus.FIRST_HALF);
            liveEventService.updateMatchMinute(fixture.getId(), 1);
            System.out.println("Match started: " + fixture.getHomeTeam().getName() + " vs " + fixture.getAwayTeam().getName());
        }
    }

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void resumeFromHalftime() {
        List<Fixture> halftimeFixtures = fixtureRepository.findByStatus(MatchStatus.HALFTIME);
        for (Fixture fixture : halftimeFixtures) {
            liveEventService.updateMatchStatus(fixture.getId(), MatchStatus.SECOND_HALF);
            liveEventService.updateMatchMinute(fixture.getId(), 46);
            System.out.println("Second half started: " + fixture.getHomeTeam().getName() + " vs " + fixture.getAwayTeam().getName());
        }
    }

    private void simulateGoal(Fixture fixture) {
        boolean isHomeGoal = random.nextBoolean();
        UUID teamId = isHomeGoal ? fixture.getHomeTeam().getId() : fixture.getAwayTeam().getId();
        int minute = fixture.getMinute();

        Player scorer = null;
        List<Player> teamPlayers = isHomeGoal
                ? fixture.getHomeTeam().getClass().equals(fixture.getHomeTeam().getClass())
                    ? List.of() : List.of()
                : List.of();

        try {
            var players = isHomeGoal
                    ? fixture.getHomeTeam().getClass().getMethod("getId").invoke(fixture.getHomeTeam())
                    : fixture.getAwayTeam().getClass().getMethod("getId").invoke(fixture.getAwayTeam());
        } catch (Exception ignored) {}

        String description = (isHomeGoal ? fixture.getHomeTeam().getShortName() : fixture.getAwayTeam().getShortName())
                + " scores! (" + minute + "')";

        try {
            liveEventService.processGoalEvent(fixture.getId(), teamId, null, minute, description);
        } catch (Exception e) {
            System.err.println("Goal simulation error: " + e.getMessage());
        }
    }

    private void simulateCard(Fixture fixture) {
        boolean isHome = random.nextBoolean();
        UUID teamId = isHome ? fixture.getHomeTeam().getId() : fixture.getAwayTeam().getId();
        String teamName = isHome ? fixture.getHomeTeam().getShortName() : fixture.getAwayTeam().getShortName();

        boolean isRed = random.nextInt(100) < 20;
        LiveEventType cardType = isRed ? LiveEventType.RED_CARD : LiveEventType.YELLOW_CARD;
        String description = teamName + " receives a " + (isRed ? "RED" : "YELLOW") + " card (" + fixture.getMinute() + "')";

        try {
            liveEventService.processCardEvent(fixture.getId(), teamId, null, fixture.getMinute(), cardType, description);
        } catch (Exception e) {
            System.err.println("Card simulation error: " + e.getMessage());
        }
    }
}
