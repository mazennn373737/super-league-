package com.superleague.service;

import com.superleague.config.RedisCacheService;
import com.superleague.dto.BracketUpdateMessage;
import com.superleague.model.*;
import com.superleague.repository.*;
import com.superleague.websocket.LiveScoreWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class KnockoutBracketService {

    private final KnockoutBracketRepository bracketRepository;
    private final FixtureRepository fixtureRepository;
    private final StandingRepository standingRepository;
    private final TeamRepository teamRepository;
    private final LiveScoreWebSocketHandler webSocketHandler;
    private final RedisCacheService redisCache;

    private static final String BRACKET_KEY_PREFIX = "live:bracket:";
    private static final long CACHE_TTL = 3600;

    public KnockoutBracketService(KnockoutBracketRepository bracketRepository, FixtureRepository fixtureRepository,
                                  StandingRepository standingRepository, TeamRepository teamRepository,
                                  LiveScoreWebSocketHandler webSocketHandler, RedisCacheService redisCache) {
        this.bracketRepository = bracketRepository;
        this.fixtureRepository = fixtureRepository;
        this.standingRepository = standingRepository;
        this.teamRepository = teamRepository;
        this.webSocketHandler = webSocketHandler;
        this.redisCache = redisCache;
    }

    @Transactional
    public void initializeBracket(Division division) {
        List<Standing> standings = standingRepository.findByDivisionOrderByPointsDescGoalDifferenceDescGoalsForDesc(division);
        List<Team> topTeams = standings.stream()
                .map(Standing::getTeam)
                .limit(8)
                .toList();

        if (topTeams.size() >= 8) {
            createKnockoutMatch(division, topTeams.get(0), topTeams.get(7), Stage.QUARTER_FINAL, 0);
            createKnockoutMatch(division, topTeams.get(1), topTeams.get(6), Stage.QUARTER_FINAL, 1);
            createKnockoutMatch(division, topTeams.get(2), topTeams.get(5), Stage.QUARTER_FINAL, 2);
            createKnockoutMatch(division, topTeams.get(3), topTeams.get(4), Stage.QUARTER_FINAL, 3);
        } else if (topTeams.size() >= 4) {
            createKnockoutMatch(division, topTeams.get(0), topTeams.get(3), Stage.SEMI_FINAL, 0);
            createKnockoutMatch(division, topTeams.get(1), topTeams.get(2), Stage.SEMI_FINAL, 1);
        }
    }

    private KnockoutBracket createKnockoutMatch(Division division, Team home, Team away, Stage stage, int position) {
        Fixture fixture = new Fixture(home, away, division, null, stage, null);
        fixtureRepository.save(fixture);
        KnockoutBracket bracket = new KnockoutBracket(fixture, stage, position);
        return bracketRepository.save(bracket);
    }

    @Transactional
    public void processFinishedFixture(Fixture fixture) {
        if (fixture.getStage() == Stage.GROUP) return;

        List<KnockoutBracket> brackets = bracketRepository.findByFixtureId(fixture.getId());
        if (brackets.isEmpty()) return;

        KnockoutBracket currentBracket = brackets.get(0);
        Team winner = fixture.getHomeScore() > fixture.getAwayScore()
                ? fixture.getHomeTeam() : fixture.getAwayTeam();
        Team loser = fixture.getHomeScore() > fixture.getAwayScore()
                ? fixture.getAwayTeam() : fixture.getHomeTeam();

        List<KnockoutBracket> allBrackets = bracketRepository.findAll();
        for (KnockoutBracket nextBracket : allBrackets) {
            if (nextBracket.getParentWinner() != null &&
                nextBracket.getParentWinner().getId().equals(currentBracket.getId())) {
                updateBracketFixtureTeam(nextBracket, winner, true);
            }
            if (nextBracket.getParentLoser() != null &&
                nextBracket.getParentLoser().getId().equals(currentBracket.getId())) {
                updateBracketFixtureTeam(nextBracket, loser, false);
            }
        }

        broadcastBracket();
    }

    private void updateBracketFixtureTeam(KnockoutBracket bracket, Team team, boolean isWinner) {
        Fixture fixture = bracket.getFixture();
        if (fixture.getHomeTeam() == null || fixture.getHomeTeam().getName().contains("TBD")) {
            fixture.setHomeTeam(team);
        } else if (fixture.getAwayTeam() == null || fixture.getAwayTeam().getName().contains("TBD")) {
            fixture.setAwayTeam(team);
        }
        fixtureRepository.save(fixture);
    }

    @SuppressWarnings("unchecked")
    public List<BracketUpdateMessage.BracketNode> getBracket(Stage stage) {
        String cacheKey = BRACKET_KEY_PREFIX + stage.name();
        List<BracketUpdateMessage.BracketNode> cached = redisCache.get(cacheKey);
        if (cached != null) return cached;

        List<KnockoutBracket> brackets = bracketRepository.findByStageOrderByPosition(stage);
        List<BracketUpdateMessage.BracketNode> nodes = brackets.stream().map(b -> {
            Fixture f = b.getFixture();
            return new BracketUpdateMessage.BracketNode(
                b.getId(), f.getId(),
                f.getHomeTeam().getName(), f.getAwayTeam().getName(),
                f.getHomeTeam().getShortName(), f.getAwayTeam().getShortName(),
                f.getHomeScore(), f.getAwayScore(),
                b.getPosition(),
                b.getParentWinner() != null ? b.getParentWinner().getId() : null,
                b.getParentLoser() != null ? b.getParentLoser().getId() : null,
                f.getStatus().name()
            );
        }).toList();

        redisCache.set(cacheKey, (Object) nodes, CACHE_TTL, TimeUnit.SECONDS);
        return nodes;
    }

    public void broadcastBracket() {
        for (Stage stage : List.of(Stage.QUARTER_FINAL, Stage.SEMI_FINAL, Stage.FINAL)) {
            List<BracketUpdateMessage.BracketNode> nodes = getBracket(stage);
            BracketUpdateMessage message = new BracketUpdateMessage(stage, nodes);
            webSocketHandler.broadcast(message);
        }
    }
}
