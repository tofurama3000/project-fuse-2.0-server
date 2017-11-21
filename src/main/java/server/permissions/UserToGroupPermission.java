package server.permissions;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.permissions.results.JoinResult;

import static server.constants.RoleValue.*;
import static server.permissions.results.JoinResult.*;

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
      return ALREADY_JOINED;
    }

    switch (group.getRestriction()) {
      case INVITE:
        if (hasInvite()) {
          return HAS_INVITE;
        } else {
          return NEED_INVITE;
        }
      case NONE:
      default:
        return OK;
    }
  }

  protected abstract Session getSession();

  @Deprecated
  protected abstract String getGroupFieldName();


  protected boolean isMember() {
    for (Integer roleId : getRoles()) {
      if (roleId != INVITED) {
        return true;
      }
    }
    return false;
  }

  protected boolean hasInvite() {
    for (Integer roleId : getRoles()) {
      if (roleId == INVITED) {
        return true;
      }
    }
    return false;
  }

  public boolean canInvite() {
    return isAdmin();
  }

  public boolean canAcceptInvite() {
    // Just checking if there already exists a role for user
    for (Integer ignored : getRoles()) {
      return false;
    }
    return true;
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
