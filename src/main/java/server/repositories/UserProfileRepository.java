package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.User;
import server.entities.dto.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
}
