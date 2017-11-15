package server.repositories.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.User;
import server.entities.dto.project.Project;
import server.entities.dto.team.Team;


public interface ProjectRepository extends CrudRepository<Project, Long> {
  @Query("From Project t WHERE t.owner =:owner AND t.name=:name")
  Iterable<Project> getProjects(@Param("owner") User user, @Param("name") String name);
}
