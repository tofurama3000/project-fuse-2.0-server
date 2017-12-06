package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.FuseSession;

public interface SessionRepository extends CrudRepository<FuseSession, String> {
}
