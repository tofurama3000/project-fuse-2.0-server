package server.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class LocalMailSenderImpl implements MailSender {

  Logger logger = LoggerFactory.getLogger(LocalMailSenderImpl.class);

  @Autowired
  private JavaMailSender emailSender;

  @Override
  public void sendBasicEmail(String from, String to, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);

    new Thread(() -> emailSender.send(message)).start();
  }

  @Override
  public void sendHtmlEmail(String from, String to, String subject, String body) {
    MimeMessage mimeMessage = emailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
      mimeMessage.setContent(body, "text/html");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom(from);
    } catch (MessagingException e) {
      logger.error("Could not create message", e);
      return;
    }

    new Thread(() -> emailSender.send(mimeMessage)).start();
  }
}
