package com.superleague.service;

import com.superleague.config.RedisCacheService;
import com.superleague.dto.StandingUpdateMessage;
import com.superleague.model.*;
import com.superleague.repository.*;
import com.superleague.websocket.LiveScoreWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class StandingService {

    private final StandingRepository standingRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamRepository teamRepository;
    private final LiveScoreWebSocketHandler webSocketHandler;
    private final RedisCacheService redisCache;

    private static final String STANDINGS_KEY_PREFIX = "live:standings:";
    private static final long CACHE_TTL = 3600;

    public StandingService(StandingRepository standingRepository, FixtureRepository fixtureRepository,
                          TeamRepository teamRepository, LiveScoreWebSocketHandler webSocketHandler,
                          RedisCacheService redisCache) {
        this.standingRepository = standingRepository;
        this.fixtureRepository = fixtureRepository;
        this.teamRepository = teamRepository;
        this.webSocketHandler = webSocketHandler;
        this.redisCache = redisCache;
    }

    @Transactional
    public void recalculateStandings(Division division, String groupName) {
        List<Fixture> groupFixtures = fixtureRepository.findByDivision(division).stream()
                .filter(f -> f.getStatus() == MatchStatus.FINISHED)
                .filter(f -> groupName == null || groupName.equals(f.getGroupName()))
                .toList();

        if (groupFixtures.isEmpty()) return;

        Set<Team> teamsInGroup = new HashSet<>();
        for (Fixture f : groupFixtures) {
            teamsInGroup.add(f.getHomeTeam());
            teamsInGroup.add(f.getAwayTeam());
        }

        standingsByGroup(division, groupFixtures, teamsInGroup, groupName);
        broadcastStandings(division);
    }

    public void initializeStandings(Division division, String groupName) {
        List<Team> teams = teamRepository.findByDivision(division);
        for (Team team : teams) {
            if (standingRepository.findByTeamIdAndDivisionAndGroupName(team.getId(), division, groupName).isEmpty()) {
                Standing standing = new Standing(team, division, groupName);
                standing.setPosition(0);
                standingRepository.save(standing);
            }
        }
    }

    private void standingsByGroup(Division division, List<Fixture> fixtures,
                                                  Set<Team> teams, String groupName) {
        Map<UUID, Standing> standingMap = new HashMap<>();

        for (Team team : teams) {
            Standing s = standingRepository
                    .findByTeamIdAndDivisionAndGroupName(team.getId(), division, groupName)
                    .orElse(new Standing(team, division, groupName));
            standingMap.put(team.getId(), s);
        }

        for (Standing s : standingMap.values()) {
            s.setPlayed(0); s.setWon(0); s.setDrawn(0); s.setLost(0);
            s.setGoalsFor(0); s.setGoalsAgainst(0); s.setGoalDifference(0); s.setPoints(0);
        }

        for (Fixture fixture : fixtures) {
            if (fixture.getStatus() != MatchStatus.FINISHED) continue;

            Standing homeStanding = standingMap.get(fixture.getHomeTeam().getId());
            Standing awayStanding = standingMap.get(fixture.getAwayTeam().getId());

            if (homeStanding == null || awayStanding == null) continue;

            homeStanding.setPlayed(homeStanding.getPlayed() + 1);
            awayStanding.setPlayed(awayStanding.getPlayed() + 1);

            homeStanding.setGoalsFor(homeStanding.getGoalsFor() + fixture.getHomeScore());
            homeStanding.setGoalsAgainst(homeStanding.getGoalsAgainst() + fixture.getAwayScore());
            awayStanding.setGoalsFor(awayStanding.getGoalsFor() + fixture.getAwayScore());
            awayStanding.setGoalsAgainst(awayStanding.getGoalsAgainst() + fixture.getHomeScore());

            if (fixture.getHomeScore() > fixture.getAwayScore()) {
                homeStanding.setWon(homeStanding.getWon() + 1);
                awayStanding.setLost(awayStanding.getLost() + 1);
            } else if (fixture.getHomeScore() < fixture.getAwayScore()) {
                awayStanding.setWon(awayStanding.getWon() + 1);
                homeStanding.setLost(homeStanding.getLost() + 1);
            } else {
                homeStanding.setDrawn(homeStanding.getDrawn() + 1);
                awayStanding.setDrawn(awayStanding.getDrawn() + 1);
            }
        }

        for (Standing s : standingMap.values()) {
            s.setPoints(s.getWon() * 3 + s.getDrawn());
            s.setGoalDifference(s.getGoalsFor() - s.getGoalsAgainst());
        }

        List<Standing> sorted = new ArrayList<>(standingMap.values());
        sorted.sort((a, b) -> {
            if (b.getPoints() != a.getPoints()) return b.getPoints() - a.getPoints();
            if (b.getGoalDifference() != a.getGoalDifference()) return b.getGoalDifference() - a.getGoalDifference();
            return b.getGoalsFor() - a.getGoalsFor();
        });

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setPosition(i + 1);
            standingRepository.save(sorted.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    public List<StandingUpdateMessage.StandingRow> getStandingsForDivision(Division division) {
        String cacheKey = STANDINGS_KEY_PREFIX + division.name();
        List<StandingUpdateMessage.StandingRow> cached = redisCache.get(cacheKey);
        if (cached != null) return cached;

        List<Standing> standings = standingRepository.findByDivisionOrderByPointsDescGoalDifferenceDescGoalsForDesc(division);
        List<StandingUpdateMessage.StandingRow> rows = standings.stream().map(s ->
            new StandingUpdateMessage.StandingRow(
                s.getTeam().getId(), s.getTeam().getName(), s.getTeam().getShortName(),
                s.getPosition(), s.getPoints(), s.getPlayed(), s.getWon(), s.getDrawn(), s.getLost(),
                s.getGoalsFor(), s.getGoalsAgainst(), s.getGoalDifference()
            )
        ).toList();

        redisCache.set(cacheKey, (Object) rows, CACHE_TTL, TimeUnit.SECONDS);
        return rows;
    }

    public void broadcastStandings(Division division) {
        List<StandingUpdateMessage.StandingRow> rows = getStandingsForDivision(division);
        StandingUpdateMessage message = new StandingUpdateMessage(division, rows);
        webSocketHandler.broadcast(message);
    }
}
