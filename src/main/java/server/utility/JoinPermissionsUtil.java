package server.utility;

import server.entities.dto.group.Group;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.UserToGroupPermission;

public class JoinPermissionsUtil {
  public static void genericSetJoinPermissions(User user, Group group, UserToGroupPermission permission) {
    group.setCanEdit(permission.canUpdate());
    switch (permission.canJoin()) {
      case OK:
        group.setCanJoin(true);
        group.setCanApply(false);
        break;
      case HAS_INVITE:
        group.setCanJoin(true);
        group.setCanApply(false);
        break;
      case NEED_INVITE:
        group.setCanJoin(false);
        group.setCanApply(true);
        break;
      case ALREADY_JOINED:
      case ERROR:
      default:
        group.setCanJoin(false);
        group.setCanApply(false);
    }
  }
}
