package server.repositories.group.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

@Transactional
public interface TeamApplicantRepository extends GroupApplicantRepository<TeamApplicant, Team> {
  @Query("FROM TeamApplicant a where a.team = :team and a.status = :status ")
  List<TeamApplicant> getApplicants(@Param("team") Team team, @Param("status") String status);
}

