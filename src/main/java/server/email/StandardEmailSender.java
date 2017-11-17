package server.email;

import static server.email.MessageFactory.REGISTRATION_SUBJECT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StandardEmailSender {

  @Autowired
  private MessageFactory messageFactory;

  @Autowired
  private MailSender mailSender;



  @Value("${spring.mail.username}")
  private String sender;

  public void sendRegistrationEmail(String to, String registrationKey) {
    String body = messageFactory.createRegistrationBody(registrationKey);
    mailSender.sendHtmlEmail(sender, to, REGISTRATION_SUBJECT, body);
  }
}
