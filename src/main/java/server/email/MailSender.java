package server.email;

import org.springframework.stereotype.Component;

@Component
public interface MailSender {
  void sendBasicEmail(String from, String to, String subject, String body);

  void sendHtmlEmail(String from, String to, String subject, String body);

}
