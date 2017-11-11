package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import server.entities.dto.Session;
import server.entities.dto.User;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import server.repositories.SessionRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class SessionController {
  private static IdGenerator generator = new AlternativeJdkIdGenerator();
  private static String SESSION_ID_NAME = "SESSIONID";

  @Autowired
  private SessionRepository sessionRepository;

  public Session createSession(User user) {
    Session session = new Session(SessionController.createId(), user);
    sessionRepository.save(session);
    return session;
  }

  public void deleteSession(Session session) {
    sessionRepository.delete(session);
  }

  public boolean isSessionValid(User user, String sessionId) {
    Session session = sessionRepository.findOne(sessionId);
    return session != null && session.getEmail().equals(user.getEmail());
  }

  public Optional<Session> getSession(HttpServletRequest servletRequest) {
    Cookie[] cookies = servletRequest.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        String name = cookie.getName();
        if (name.equals(SESSION_ID_NAME)) {
          Session session = sessionRepository.findOne(cookie.getValue());
          return Optional.of(session);
        }
      }
    }

    return Optional.empty();
  }

  private static String createId(){
    return generator.generateId().toString();
  }
}
