package com.superleague.repository;

import com.superleague.model.Division;
import com.superleague.model.Fixture;
import com.superleague.model.MatchStatus;
import com.superleague.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FixtureRepository extends JpaRepository<Fixture, UUID> {
    List<Fixture> findByDivision(Division division);
    List<Fixture> findByStatus(MatchStatus status);
    List<Fixture> findByDivisionAndStatus(Division division, MatchStatus status);
    List<Fixture> findByDivisionAndStage(Division division, Stage stage);
    List<Fixture> findByStage(Stage stage);
    List<Fixture> findByStatusNot(MatchStatus status);
}
