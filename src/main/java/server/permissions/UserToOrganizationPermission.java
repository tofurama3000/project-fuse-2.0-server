package server.permissions;

import server.entities.dto.User;
import server.entities.dto.organization.Organization;

public class UserToOrganizationPermission extends UserToGroupPermission<Organization> {

  public UserToOrganizationPermission(User user, Organization group) {
    super(user, group);
  }

  @Override
  public boolean canJoin() {
    return false;
  }
}
