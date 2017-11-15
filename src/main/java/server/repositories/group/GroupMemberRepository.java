package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.GroupMember;
import server.entities.dto.group.Group;
import server.entities.dto.User;


@NoRepositoryBean
public interface GroupMemberRepository<R extends Group, T extends GroupMember<R>> extends CrudRepository<T, Long> {
  Iterable<User> getUsersByGroup(@Param("group") R group);
}
