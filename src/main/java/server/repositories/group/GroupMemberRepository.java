package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupMember;
import server.entities.dto.user.User;

import java.util.List;


@NoRepositoryBean
public interface GroupMemberRepository<R extends Group, T extends GroupMember<R>> extends CrudRepository<T, Long> {
  Iterable<User> getUsersByGroup(@Param("group") R group);

  Iterable<Integer> getRoles(@Param("group") R group, @Param("user") User user);

  List<R> getGroups(@Param("user") User member, @Param("roleId") int roleId);

  void delete(@Param("group") R group, @Param("user") User user, @Param("roleId") int roleId);
}
