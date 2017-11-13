package server.permissions;

import server.entities.Group;
import server.entities.dto.User;

public abstract class UserToGroupPermission<T extends Group> {

  private final User user;
  private final T group;

  public UserToGroupPermission(User user, T group) {
    this.user = user;
    this.group = group;
  }

  public abstract boolean canJoin();

}
