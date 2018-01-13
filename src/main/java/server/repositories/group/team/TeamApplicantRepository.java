package server.repositories.group.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.team.TeamApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.time.LocalDateTime;
import java.util.List;

import static server.constants.Availability.AVAILABLE;

public interface TeamApplicantRepository extends GroupApplicantRepository<TeamApplicant> {
  @Query("FROM TeamApplicant a where a.team = :team and a.status = :status ")
  List<TeamApplicant> getTeamApplicants(@Param("team") Long groupId, @Param("status") String status);
}

