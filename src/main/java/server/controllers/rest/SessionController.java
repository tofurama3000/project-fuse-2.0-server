package server.controllers.rest;

import server.entities.dto.Session;
import server.entities.dto.User;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;


public class SessionController {
  private static IdGenerator generator = new AlternativeJdkIdGenerator();

  public static Session GetSession(User user){
    return new Session(SessionController.CreateId(), user);
  }

  private static String CreateId(){
    return generator.generateId().toString();
  }
}
