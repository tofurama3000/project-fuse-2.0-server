package server.repositories.group.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamMember;
import server.repositories.group.GroupMemberRepository;

@Transactional
public interface TeamMemberRepository extends GroupMemberRepository<Team, TeamMember> {
  @Query("SELECT user FROM TeamMember a where a.team = :group")
  Iterable<User> getUsersByGroup(@Param("group") Team group);
}
