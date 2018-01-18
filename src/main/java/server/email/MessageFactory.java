package server.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageFactory {
  public static final String REGISTRATION_SUBJECT = "Register your account";

  @Value("${fuse.baseUrl}")
  private String baseUrl;

  public String createRegistrationBody(String registrationKey) {
    return "<p>Register with this link: </p>" + createLink(baseUrl + "/users/register/" + registrationKey, "Register");
  }

  private String createLink(String url, String text) {
    return "<a href=\"" + url + "\">" + text + "</a>\n";
  }

}
