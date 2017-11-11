package server.repositories;

import server.entities.dto.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
  User findByEmail(String email);
}
