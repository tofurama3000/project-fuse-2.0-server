package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.Team;

public interface TeamRespository extends CrudRepository<Team, Long> {
}
