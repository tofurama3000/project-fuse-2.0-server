package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.user.User;
import server.entities.dto.group.Group;

import java.util.List;

@NoRepositoryBean
public interface GroupRepository<T extends Group> extends CrudRepository<T, Long> {
  List<T> getGroups(@Param("owner") User user, @Param("name") String name);

  List<T> getGroupsByOwner(@Param("owner") User user);
}
