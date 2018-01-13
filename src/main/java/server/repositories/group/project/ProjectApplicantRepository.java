package server.repositories.group.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.project.ProjectApplicant;
import server.entities.dto.group.team.TeamApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

public interface ProjectApplicantRepository extends GroupApplicantRepository<ProjectApplicant> {
  @Query("FROM ProjectApplicant a where a.project = :project and a.status = :status ")
  List<TeamApplicant> getProjectApplicants(@Param("project") Long groupId, @Param("status") String status);
}
