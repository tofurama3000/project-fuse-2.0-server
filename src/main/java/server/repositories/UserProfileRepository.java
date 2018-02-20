package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.user.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {
}
