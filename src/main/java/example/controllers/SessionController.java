package example.controllers;

import example.dto.Session;
import example.dto.User;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;

/**
 * Created by tofurama on 11/8/17.
 */
public class SessionController {
  private static IdGenerator generator = new AlternativeJdkIdGenerator();

  public static Session GetSession(User user){
    return new Session(SessionController.CreateId(), user);
  }

  private static String CreateId(){
    return generator.generateId().toString();
  }
}
