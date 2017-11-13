package server.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.FuseSessionController;
import server.entities.dto.User;

@Component
public class PermissionFactory {

  @Autowired
  private FuseSessionController fuseSessionController;

  public UserPermission createUserPermission(User user) {
    UserPermission permission = new UserPermission(user);
    permission.setFuseSessionController(fuseSessionController);
    return permission;
  }

}
