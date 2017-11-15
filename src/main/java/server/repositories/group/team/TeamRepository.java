package server.repositories.group.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.team.Team;
import server.entities.dto.User;
import server.repositories.group.GroupRepository;

@Transactional
public interface TeamRepository extends GroupRepository<Team> {
  @Query("From Team t WHERE t.owner =:owner AND t.name=:name")
  Iterable<Team> getGroups(@Param("owner") User user, @Param("name") String name);
}
