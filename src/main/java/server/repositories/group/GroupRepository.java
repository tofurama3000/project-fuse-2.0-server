package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.Group;
import server.entities.dto.User;

@NoRepositoryBean
public interface GroupRepository<T extends Group> extends CrudRepository<T, Long> {
  Iterable<T> getGroups(@Param("owner") User user, @Param("name") String name);
}
