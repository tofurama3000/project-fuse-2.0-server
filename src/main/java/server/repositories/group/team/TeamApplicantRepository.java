package server.repositories.group.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.time.LocalDateTime;
import java.util.List;

import static server.constants.Availability.AVAILABLE;

@Transactional
public interface TeamApplicantRepository extends GroupApplicantRepository<TeamApplicant,Team> {
  @Query("FROM TeamApplicant a where a.team = :team and a.status = :status ")
  List<TeamApplicant> getApplicants(@Param("team") Team team, @Param("status") String status);
}

