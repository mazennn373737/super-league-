package com.superleague.repository;

import com.superleague.model.Division;
import com.superleague.model.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StandingRepository extends JpaRepository<Standing, UUID> {
    List<Standing> findByDivisionOrderByPointsDescGoalDifferenceDescGoalsForDesc(Division division);
    List<Standing> findByDivisionAndGroupNameOrderByPosition(Division division, String groupName);
    Optional<Standing> findByTeamIdAndDivisionAndGroupName(UUID teamId, Division division, String groupName);
    void deleteByDivisionAndGroupName(Division division, String groupName);
}
