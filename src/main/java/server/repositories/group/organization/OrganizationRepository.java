package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.user.User;
import server.entities.dto.group.organization.Organization;
import server.repositories.group.GroupRepository;

import java.util.List;

@Transactional
public interface OrganizationRepository extends GroupRepository<Organization> {
  @Query("From Organization t WHERE t.owner =:owner AND t.name=:name")
  List<Organization> getGroups(@Param("owner") User user, @Param("name") String name);
}
