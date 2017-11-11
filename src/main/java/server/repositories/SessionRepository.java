package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.Session;

public interface SessionRepository extends CrudRepository<Session, String> {
}
