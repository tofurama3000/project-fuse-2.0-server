package server.email;

import org.springframework.stereotype.Component;

@Component
public interface MailSender {
  void sendMail(String from, String to, String subject, String body);
}
