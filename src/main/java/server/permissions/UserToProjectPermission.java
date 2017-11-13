package server.permissions;

import server.entities.dto.User;
import server.entities.dto.project.Project;

public class UserToProjectPermission extends UserToGroupPermission<Project> {

  public UserToProjectPermission(User user, Project group) {
    super(user, group);
  }

  @Override
  public boolean canJoin() {
    return false;
  }
}
