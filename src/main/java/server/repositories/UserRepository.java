package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);
}
