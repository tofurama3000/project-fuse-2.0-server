package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.UnregisteredUser;

public interface UnregisteredUserRepository extends CrudRepository<UnregisteredUser, Long> {
}
