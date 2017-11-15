package server.repositories.team;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.team.Team;
import server.entities.dto.User;

import java.util.List;

public interface TeamRepository extends CrudRepository<Team, Long> {
  @Query("From Team t WHERE t.owner =:owner AND t.name=:name")
  Iterable<Team> getTeams(@Param("owner") User user, @Param("name") String name);
}
