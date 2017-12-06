package mail;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import server.Application;
import server.config.Dependencies;
import server.email.MailSender;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {Dependencies.class, Application.class})
@ComponentScan("framework")
public class MailTest {

  @Autowired
  private MailSender mailSender;

  @Test
  public void sendSimpleEmail() {
    mailSender.sendBasicEmail("me@gmail.com", "cole.gordon57@gmail.com", "test", "test");
  }
}
