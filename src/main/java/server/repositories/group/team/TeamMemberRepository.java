package server.repositories.group.team;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamMember;
import server.repositories.group.GroupMemberRepository;

import java.util.List;

@Transactional
public interface TeamMemberRepository extends GroupMemberRepository<Team, TeamMember> {
  @Query("SELECT user FROM TeamMember a where a.team = :group")
  List<User> getUsersByGroup(@Param("group") Team group);

  @Query("SELECT roleId FROM TeamMember a where a.team = :group AND a.user = :user")
  List<Integer> getRoles(@Param("group") Team group, @Param("user") User user);

  @Modifying(clearAutomatically = true)
  @Query("DELETE from TeamMember a where a.team =:group AND a.user =:user and a.roleId = :roleId")
  void delete(@Param("group") Team group, @Param("user") User user, @Param("roleId") int roleId);

}
