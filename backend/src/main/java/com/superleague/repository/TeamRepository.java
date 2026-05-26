package com.superleague.repository;

import com.superleague.model.Division;
import com.superleague.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByDivision(Division division);
}
