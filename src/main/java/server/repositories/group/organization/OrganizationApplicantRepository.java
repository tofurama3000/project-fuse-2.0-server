package server.repositories.group.organization;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.team.TeamApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

public interface OrganizationApplicantRepository extends GroupApplicantRepository<OrganizationApplicant> {
  @Query("FROM OrganizationApplicant a where a.organization = :organization and a.status = :status ")
  List<TeamApplicant> getTeamApplicants(@Param("organization") Long groupId, @Param("status") String status);
}
