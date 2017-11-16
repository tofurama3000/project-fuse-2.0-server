package server.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class LocalMailSenderImpl implements MailSender {

  @Autowired
  private JavaMailSender emailSender;

  @Override
  public void sendMail(String from, String to, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);

    emailSender.send(message);
  }
}
