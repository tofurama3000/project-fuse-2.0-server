package server.entities;

import server.controllers.SessionController;
import server.entities.dto.FuseSession;
import server.entities.dto.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class UserPermission {

  private final User user;
  private final HttpServletRequest httpServletRequest;
  private final SessionController sessionController;

  public UserPermission(User user, HttpServletRequest httpServletRequest, SessionController sessionController) {
    this.user = user;
    this.httpServletRequest = httpServletRequest;
    this.sessionController = sessionController;
  }

  public boolean isLoggedIn() {
    Optional<FuseSession> session = sessionController.getSession(httpServletRequest);
    if (session.isPresent()) {
      String sessionId = session.get().getSessionId();
      return sessionController.isSessionValid(user, sessionId);
    } else {
      return false;
    }
  }

}
