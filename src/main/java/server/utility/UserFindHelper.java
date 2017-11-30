package server.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.entities.dto.User;
import server.repositories.UserRepository;

import java.util.Optional;

@Service
public class UserFindHelper {

  @Autowired
  private UserRepository userRepository;

  public Optional<User> findUserByEmailIfIdNotSet(User user) {

    if (user == null) {
      return Optional.empty();
    }

    if (user.getId() == null) {
      if (user.getEmail() == null) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(userRepository.findByEmail(user.getEmail()));
      }
    } else {
      return Optional.of(user);
    }
  }
}
