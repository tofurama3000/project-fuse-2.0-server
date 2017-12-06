package framework;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.Application;
import server.config.Dependencies;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {Dependencies.class, Application.class})
@ComponentScan("framework")
@Transactional
@AutoConfigureMockMvc
public abstract class RestTester {

  @Autowired
  private MockMvc mockMvc;





}
