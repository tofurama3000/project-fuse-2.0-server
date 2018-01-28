package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
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

  public boolean isSessionValid(User user, String sessionId) {
    FuseSession fuseSession = sessionRepository.findOne(sessionId);
    return fuseSession != null && fuseSession.getUser().getId() == user.getId();
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

  private static String createId() {
    return generator.generateId().toString();
  }
}
