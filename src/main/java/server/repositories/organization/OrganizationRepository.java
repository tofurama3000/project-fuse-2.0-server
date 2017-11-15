package server.repositories.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.User;
import server.entities.dto.organization.Organization;
import server.entities.dto.team.Team;


public interface OrganizationRepository extends CrudRepository<Organization, Long> {
  @Query("From Organization t WHERE t.owner =:owner AND t.name=:name")
  Iterable<Organization> getOrganizations(@Param("owner") User user, @Param("name") String name);
}
