package framework;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import server.Application;
import server.config.Dependencies;

import java.io.File;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {Dependencies.class, Application.class})
@Transactional
@AutoConfigureMockMvc
public abstract class RestTester {

  protected String getContentsFromResources(String path) throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(path).getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }
}
