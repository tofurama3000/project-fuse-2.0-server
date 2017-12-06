package server.entities.user_to_group.permissions;

import lombok.Setter;
import server.controllers.FuseSessionController;
import server.entities.dto.FuseSession;
import server.entities.dto.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class UserPermission {

  private final User user;

  @Setter
  private FuseSessionController fuseSessionController;

  public UserPermission(User user) {
    this.user = user;
  }

  public boolean isLoggedIn(HttpServletRequest httpServletRequest) {
    Optional<FuseSession> session = fuseSessionController.getSession(httpServletRequest);
    if (session.isPresent()) {
      String sessionId = session.get().getSessionId();
      return fuseSessionController.isSessionValid(user, sessionId);
    } else {
      return false;
    }
  }

}
