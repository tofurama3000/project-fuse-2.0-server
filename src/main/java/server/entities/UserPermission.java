package server.entities;

import server.controllers.FuseSessionController;
import server.entities.dto.FuseSession;
import server.entities.dto.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class UserPermission {

  private final User user;
  private final HttpServletRequest httpServletRequest;
  private final FuseSessionController fuseSessionController;

  public UserPermission(User user, HttpServletRequest httpServletRequest, FuseSessionController fuseSessionController) {
    this.user = user;
    this.httpServletRequest = httpServletRequest;
    this.fuseSessionController = fuseSessionController;
  }

  public boolean isLoggedIn() {
    Optional<FuseSession> session = fuseSessionController.getSession(httpServletRequest);
    if (session.isPresent()) {
      String sessionId = session.get().getSessionId();
      return fuseSessionController.isSessionValid(user, sessionId);
    } else {
      return false;
    }
  }

}
