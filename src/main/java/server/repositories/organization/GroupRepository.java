package server.repositories.organization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.User;
import server.entities.dto.organization.Organization;

public interface GroupRepository<T> extends CrudRepository<T, Long> {
  Iterable<Organization> getOrganizations(@Param("owner") User user, @Param("name") String name);
}
