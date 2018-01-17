package server.entities.user_to_group.permissions;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.DEFAULT_USER;
import static server.constants.RoleValue.INVITED_TO_JOIN;
import static server.constants.RoleValue.OWNER;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.user_to_group.permissions.results.JoinResult;


@Transactional
public abstract class UserToGroupPermission<T extends Group> {

  protected final User user;
  protected final T group;

  public UserToGroupPermission(User user, T group) {
    this.user = user;
    this.group = group;
  }

  public JoinResult canJoin() {
    if (isMember()) {
      return JoinResult.ALREADY_JOINED;
    }

    switch (group.getRestriction()) {
      case INVITE:
        if (hasInvite()) {
          return JoinResult.HAS_INVITE;
        } else {
          return JoinResult.NEED_INVITE;
        }
      case NONE:
      default:
        return JoinResult.OK;
    }
  }

  protected abstract Session getSession();

  @Deprecated
  protected abstract String getGroupFieldName();


  public boolean isMember() {
    for (Integer roleId : getRoles()) {
      if (roleId == ADMIN || roleId == DEFAULT_USER) {
        return true;
      }
    }
    return false;
  }

  protected boolean hasInvite() {
    for (Integer roleId : getRoles()) {
      if (roleId == INVITED_TO_JOIN) {
        return true;
      }
    }
    return false;
  }

  public boolean canInvite() {
    return isAdmin();
  }

  public boolean hasRole(int roleToCheck) {
    for (Integer role : getRoles()) {
      if (role == roleToCheck) {
        return true;
      }
    }
    return false;
  }


  protected abstract Iterable<Integer> getRoles();

  public boolean canUpdate() {
    return isAdmin();
  }

  private boolean isAdmin() {
    for (Integer roleId : getRoles()) {
      if (roleId == ADMIN || roleId == OWNER) {
        return true;
      }
    }
    return false;
  }
}
