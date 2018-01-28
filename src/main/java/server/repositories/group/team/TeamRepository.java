package server.repositories.group.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.user.User;
import server.entities.dto.group.team.Team;
import server.repositories.group.GroupRepository;

import java.util.List;

@Transactional
public interface TeamRepository extends GroupRepository<Team> {
  @Query("From Team t WHERE t.owner =:owner AND t.name=:name")
  List<Team> getGroups(@Param("owner") User user, @Param("name") String name);
}
