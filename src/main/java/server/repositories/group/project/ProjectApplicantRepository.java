package server.repositories.group.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplication;
import server.entities.dto.user.User;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

@Transactional
public interface ProjectApplicantRepository extends GroupApplicantRepository<ProjectApplication, Project> {
  @Query("FROM ProjectApplication a where a.project = :project and a.status = :status ")
  List<ProjectApplication> getApplicants(@Param("project") Project project, @Param("status") String status);

  @Query("FROM ProjectApplication a where a.status = :status ")
  List<ProjectApplication> getApplicantsByStatus(@Param("status") String status);

  @Query("SELECT count(a.id) FROM ProjectApplication a where a.project = :project and " +
      "a.status <> 'accepted' and a.status <> 'decline' and a.sender = :user")
  Integer getNumApplications(@Param("project") Project project, @Param("user") User user);
}
