package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.team.Team;

public interface TeamRepository extends CrudRepository<Team, Long> {
}
