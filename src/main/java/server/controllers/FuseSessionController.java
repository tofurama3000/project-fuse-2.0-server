package server.controllers;

import static server.controllers.rest.response.BaseResponse.Status.DENIED;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import server.controllers.rest.errors.DeniedException;
import server.entities.PossibleError;
import server.entities.dto.FuseSession;
import server.entities.dto.user.User;
import server.repositories.SessionRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class FuseSessionController {
  private static IdGenerator generator = new AlternativeJdkIdGenerator();
  private static String SESSION_ID_NAME = "SESSIONID";

  @Autowired
  private SessionRepository sessionRepository;

  public FuseSession createSession(User user) {
    FuseSession fuseSession = new FuseSession(FuseSessionController.createId(), user);
    sessionRepository.save(fuseSession);
    return fuseSession;
  }

  public void deleteSession(FuseSession fuseSession) {
    sessionRepository.delete(fuseSession);
  }

  public PossibleError validateSession(HttpServletRequest request) {
    Optional<FuseSession> session = getSession(request);
    if (!session.isPresent()) {
      return new PossibleError(INVALID_SESSION, DENIED);
    } else {
      return new PossibleError(OK);
    }
  }

  public boolean isSessionValid(User user, String sessionId) {
    FuseSession fuseSession = sessionRepository.findOne(sessionId);
    return fuseSession != null && fuseSession.getUser().getId().equals(user.getId());
  }

  public boolean isSessionValid(HttpServletRequest servletRequest) {
    Optional<FuseSession> session = getSession(servletRequest);
    return session.isPresent();
  }

  public Optional<FuseSession> getSession(HttpServletRequest servletRequest) {
    String sessionId = servletRequest.getHeader(SESSION_ID_NAME);
    if (sessionId != null) {
      FuseSession fuseSession = sessionRepository.findOne(sessionId);
      return Optional.ofNullable(fuseSession);
    }
    return Optional.empty();
  }

  public User getUserFromSession(HttpServletRequest servletRequest) throws InvalidSessionException {
    return getSession(servletRequest).map(FuseSession::getUser).orElseThrow(InvalidSessionException::new);

  }

  private class InvalidSessionException extends DeniedException {

    InvalidSessionException() {
      super(INVALID_SESSION);
    }
  }

  private static String createId() {
    return generator.generateId().toString();
  }
}
