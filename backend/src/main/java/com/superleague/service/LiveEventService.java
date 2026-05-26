package com.superleague.service;

import com.superleague.config.RedisCacheService;
import com.superleague.dto.FixtureUpdateMessage;
import com.superleague.model.*;
import com.superleague.repository.*;
import com.superleague.websocket.LiveScoreWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LiveEventService {

    private final FixtureRepository fixtureRepository;
    private final LiveEventRepository liveEventRepository;
    private final LiveScoreWebSocketHandler webSocketHandler;
    private final RedisCacheService redisCache;
    private final StandingService standingService;
    private final KnockoutBracketService knockoutBracketService;

    private static final String FIXTURE_KEY_PREFIX = "live:fixture:";
    private static final long CACHE_TTL = 3600;

    public LiveEventService(FixtureRepository fixtureRepository, LiveEventRepository liveEventRepository,
                           LiveScoreWebSocketHandler webSocketHandler, RedisCacheService redisCache,
                           StandingService standingService, KnockoutBracketService knockoutBracketService) {
        this.fixtureRepository = fixtureRepository;
        this.liveEventRepository = liveEventRepository;
        this.webSocketHandler = webSocketHandler;
        this.redisCache = redisCache;
        this.standingService = standingService;
        this.knockoutBracketService = knockoutBracketService;
    }

    @Transactional
    public FixtureUpdateMessage processGoalEvent(UUID fixtureId, UUID teamId, UUID playerId,
                                                  int minute, String description) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + fixtureId));

        Team team = null;
        if (teamId != null) {
            team = fixture.getHomeTeam().getId().equals(teamId) ? fixture.getHomeTeam() : fixture.getAwayTeam();
        }

        Player player = null;
        if (playerId != null) {
            player = new Player();
            player.setId(playerId);
        }

        LiveEvent event = new LiveEvent(fixture, LiveEventType.GOAL, team, player, minute, description);
        liveEventRepository.save(event);

        if (team != null && team.getId().equals(fixture.getHomeTeam().getId())) {
            fixture.setHomeScore(fixture.getHomeScore() + 1);
        } else if (team != null) {
            fixture.setAwayScore(fixture.getAwayScore() + 1);
        }
        fixtureRepository.save(fixture);

        FixtureUpdateMessage message = buildFixtureUpdate(fixture);
        cacheFixture(fixture);
        webSocketHandler.broadcast(message);

        return message;
    }

    @Transactional
    public FixtureUpdateMessage processCardEvent(UUID fixtureId, UUID teamId, UUID playerId,
                                                  int minute, LiveEventType cardType, String description) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + fixtureId));

        Team team = null;
        if (teamId != null) {
            team = fixture.getHomeTeam().getId().equals(teamId) ? fixture.getHomeTeam() : fixture.getAwayTeam();
        }

        Player player = null;
        if (playerId != null) {
            player = new Player();
            player.setId(playerId);
        }

        LiveEvent event = new LiveEvent(fixture, cardType, team, player, minute, description);
        liveEventRepository.save(event);

        FixtureUpdateMessage message = buildFixtureUpdate(fixture);
        cacheFixture(fixture);
        webSocketHandler.broadcast(message);

        return message;
    }

    @Transactional
    public FixtureUpdateMessage updateMatchMinute(UUID fixtureId, int minute) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + fixtureId));

        fixture.setMinute(minute);
        fixtureRepository.save(fixture);

        FixtureUpdateMessage message = buildFixtureUpdate(fixture);
        cacheFixture(fixture);
        webSocketHandler.broadcast(message);

        return message;
    }

    @Transactional
    public FixtureUpdateMessage updateMatchStatus(UUID fixtureId, MatchStatus newStatus) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + fixtureId));

        MatchStatus oldStatus = fixture.getStatus();
        fixture.setStatus(newStatus);
        fixtureRepository.save(fixture);

        if (newStatus == MatchStatus.FINISHED && oldStatus != MatchStatus.FINISHED) {
            standingService.recalculateStandings(fixture.getDivision(), fixture.getGroupName());
            knockoutBracketService.processFinishedFixture(fixture);
        }

        FixtureUpdateMessage message = buildFixtureUpdate(fixture);
        cacheFixture(fixture);
        webSocketHandler.broadcast(message);

        return message;
    }

    public FixtureUpdateMessage getFixtureUpdate(UUID fixtureId) {
        String cacheKey = FIXTURE_KEY_PREFIX + fixtureId;
        FixtureUpdateMessage cached = redisCache.get(cacheKey);
        if (cached != null) return cached;

        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + fixtureId));
        FixtureUpdateMessage message = buildFixtureUpdate(fixture);
        cacheFixture(fixture);
        return message;
    }

    private FixtureUpdateMessage buildFixtureUpdate(Fixture fixture) {
        List<LiveEvent> events = liveEventRepository.findByFixtureIdOrderByMinuteAsc(fixture.getId());
        return new FixtureUpdateMessage(fixture, events);
    }

    private void cacheFixture(Fixture fixture) {
        String cacheKey = FIXTURE_KEY_PREFIX + fixture.getId();
        FixtureUpdateMessage message = buildFixtureUpdate(fixture);
        redisCache.set(cacheKey, message, CACHE_TTL, TimeUnit.SECONDS);
    }
}
