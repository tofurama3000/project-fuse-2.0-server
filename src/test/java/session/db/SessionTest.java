package session.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.Application;
import server.config.Dependencies;
import server.controllers.FuseSessionController;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.repositories.UserRepository;

import java.io.File;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {Dependencies.class, Application.class})
@Transactional
@AutoConfigureMockMvc
public class SessionTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private UserRepository userRepository;

  private User primaryUser;
  private User secondaryUser;

  @Before
  public void setup() {
    primaryUser = new User();
    primaryUser.setEmail("test@test.com");
    primaryUser.setName("test");
    primaryUser.set_password("test");
    userRepository.save(primaryUser);

    secondaryUser = new User();
    secondaryUser.setEmail("test2@test.com");
    secondaryUser.setName("test2");
    secondaryUser.set_password("test2");
    userRepository.save(secondaryUser);
  }

  @Test
  public void checkSessionLifetime() {

    FuseSession session = fuseSessionController.createSession(primaryUser);
    assertTrue(fuseSessionController.isSessionValid(primaryUser, session.getSessionId()));

    fuseSessionController.deleteSession(session);
    assertFalse(fuseSessionController.isSessionValid(primaryUser, session.getSessionId()));
  }

  @Test
  public void sessionNotValidForOtherUser() {

    FuseSession session = fuseSessionController.createSession(primaryUser);
    assertFalse(fuseSessionController.isSessionValid(secondaryUser, session.getSessionId()));
  }

  @Test(expected = JpaObjectRetrievalFailureException.class)
  public void sessionNotCreatedWithNotRealUser() {

    User fakeUser = new User();
    fakeUser.setId(100);
    fakeUser.setEmail("fakeUser@test.com");
    fakeUser.setName("fake");
    fakeUser.set_password("test");
    fuseSessionController.createSession(fakeUser);
  }



  protected String getContentsFromResources(String path) throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(path).getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }
}
