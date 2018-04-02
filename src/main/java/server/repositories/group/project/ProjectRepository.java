package server.repositories.group.project;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.GroupRepository;

import java.util.List;


@Transactional
public interface ProjectRepository extends GroupRepository<Project> {
  @Query("From Project t WHERE t.owner =:owner AND t.name=:name AND t.deleted = 0")
  List<Project> getGroups(@Param("owner") User user, @Param("name") String name);

  @Query("From Project t WHERE t.organization =:organization AND t.deleted = 0")
  List<Project> getGroupsInOrganization(@Param("organization") Organization organization);

}
