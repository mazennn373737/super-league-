package com.superleague.repository;

import com.superleague.model.KnockoutBracket;
import com.superleague.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface KnockoutBracketRepository extends JpaRepository<KnockoutBracket, UUID> {
    List<KnockoutBracket> findByStageOrderByPosition(Stage stage);
    List<KnockoutBracket> findByFixtureId(UUID fixtureId);
}
