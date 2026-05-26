package com.superleague.ingestion;

import com.superleague.model.*;
import com.superleague.repository.*;
import com.superleague.service.KnockoutBracketService;
import com.superleague.service.StandingService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class MockDataIngestionWorker {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final StandingService standingService;
    private final KnockoutBracketService knockoutBracketService;

    private static final Random RANDOM = new Random();

    public MockDataIngestionWorker(TeamRepository teamRepository, PlayerRepository playerRepository,
                                   FixtureRepository fixtureRepository, StandingService standingService,
                                   KnockoutBracketService knockoutBracketService) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.fixtureRepository = fixtureRepository;
        this.standingService = standingService;
        this.knockoutBracketService = knockoutBracketService;
    }

    @PostConstruct
    public void seedData() {
        if (teamRepository.count() > 0) return;

        List<Team> starTeams = createTeams(Division.STAR, List.of(
            "FC Phoenix", "Thunder United", "Crystal Legion", "Royal Titans",
            "Storm Breakers", "Golden Eagles", "Silver Arrows", "Dragon FC"
        ));
        List<Team> goldTeams = createTeams(Division.GOLD, List.of(
            "Ocean Warriors", "Mountain Kings", "Iron Lions", "Shadow Panthers",
            "Blaze Falcons", "Frost Giants", "Thunder Hawks", "Crimson Wolves"
        ));
        List<Team> blueTeams = createTeams(Division.BLUE, List.of(
            "Neon Chargers", "Steel Cobras", "Phantom Riders", "Wild Vipers",
            "Apex Predators", "Titan Bears", "Venom Sharks", "Cyber Hawks"
        ));

        for (Team team : starTeams) createPlayers(team);
        for (Team team : goldTeams) createPlayers(team);
        for (Team team : blueTeams) createPlayers(team);

        createGroupFixtures(starTeams, Division.STAR, "A");
        createGroupFixtures(starTeams.subList(4, 8), Division.STAR, "B");
        createGroupFixtures(goldTeams, Division.GOLD, "A");
        createGroupFixtures(blueTeams, Division.BLUE, "A");

        for (Division div : Division.values()) {
            standingService.initializeStandings(div, "A");
            if (div == Division.STAR) standingService.initializeStandings(div, "B");
        }

        System.out.println("Mock data seeded successfully!");
    }

    private List<Team> createTeams(Division division, List<String> names) {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Team team = new Team(names.get(i), names.get(i).substring(0, Math.min(3, names.get(i).length())).toUpperCase(),
                    null, division);
            teams.add(teamRepository.save(team));
        }
        return teams;
    }

    private void createPlayers(Team team) {
        String[][] positions = {
            {"GK", "1"}, {"DEF", "2"}, {"DEF", "3"}, {"DEF", "4"}, {"MID", "5"},
            {"MID", "6"}, {"MID", "7"}, {"MID", "8"}, {"FWD", "9"}, {"FWD", "10"}, {"FWD", "11"}
        };
        String[] firstNames = {"Alex", "Max", "Leo", "Kai", "Sam", "Liam", "Noah", "Ivy", "Eli", "Luna", "Mia"};
        String[] lastNames = {"Smith", "Jones", "Wong", "Park", "Chen", "Garcia", "Kim", "Patel", "Brown", "Lee", "Wang"};

        for (int i = 0; i < positions.length; i++) {
            String name = firstNames[i] + " " + lastNames[i];
            Player player = new Player(name, positions[i][0], Integer.parseInt(positions[i][1]), team);
            playerRepository.save(player);
        }
    }

    private void createGroupFixtures(List<Team> teams, Division division, String groupName) {
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Fixture fixture = new Fixture(teams.get(i), teams.get(j), division, groupName,
                        Stage.GROUP, LocalDateTime.now());
                fixtureRepository.save(fixture);
            }
        }
    }
}
