package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.user.UnregisteredUser;

public interface UnregisteredUserRepository extends CrudRepository<UnregisteredUser, Long> {
}
