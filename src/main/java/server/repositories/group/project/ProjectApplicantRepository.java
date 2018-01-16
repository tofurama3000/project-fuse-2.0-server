package server.repositories.group.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplicant;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

@Transactional
public interface ProjectApplicantRepository extends GroupApplicantRepository<ProjectApplicant,Project> {
  @Query("FROM ProjectApplicant a where a.project = :project and a.status = :status ")
  List<ProjectApplicant> getApplicants(@Param("project") Project project, @Param("status") String status);
}
