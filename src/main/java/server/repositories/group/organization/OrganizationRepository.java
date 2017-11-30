package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.organization.Organization;
import server.repositories.group.GroupRepository;

@Transactional
public interface OrganizationRepository extends GroupRepository<Organization> {
    @Query("From Organization t WHERE t.owner =:owner AND t.name=:name")
    Iterable<Organization> getGroups(@Param("owner") User user, @Param("name") String name);
}
