package server.permissions;

import static server.constants.RoleValue.INVITED;
import static server.permissions.results.JoinResult.ALREADY_JOINED;
import static server.permissions.results.JoinResult.HAS_INVITE;
import static server.permissions.results.JoinResult.NEED_INVITE;
import static server.permissions.results.JoinResult.OK;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import server.entities.Group;
import server.entities.dto.User;
import server.permissions.results.JoinResult;

import java.util.List;

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

  protected abstract String getGroupFieldName();

  protected boolean isMember() {
    String queryString = "SELECT sum(id) FROM " + group.getRelationshipTableName() + " r WHERE r." + getGroupFieldName()
        + " = :group AND r.user = :user";
    Query query = getSession().createQuery(queryString);

    query.setParameter("group", group);
    query.setParameter("user", user);

    Object result = query.uniqueResult();
    return result != null;
  }

  @SuppressWarnings("unchecked")
  protected boolean hasInvite() {
    String queryString = "SELECT role_id FROM " + group.getRelationshipTableName() + " r WHERE r." + getGroupFieldName()
        + " = :group AND r.user= :user";

    Query query = getSession().createQuery(queryString);

    query.setParameter("group", group);
    query.setParameter("user", user);

    List<Integer> roleIds = query.list();
    for (Integer roleId : roleIds) {
      if (roleId == INVITED) {
        return true;
      }
    }
    return false;
  }

}
