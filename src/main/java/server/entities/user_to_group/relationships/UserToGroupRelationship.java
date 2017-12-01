package server.entities.user_to_group.relationships;

import server.entities.dto.User;
import server.entities.dto.group.Group;

public abstract class UserToGroupRelationship<T extends Group> {

  protected final User user;
  protected final T group;

  public UserToGroupRelationship(User user, T group) {
    this.user = user;
    this.group = group;
  }

  public abstract boolean addRelationship(int role);

  public abstract boolean removeRelationship(int role);
}
