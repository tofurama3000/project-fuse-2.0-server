package framework;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {Dependencies.class, Application.class})
@Transactional
@AutoConfigureMockMvc
public abstract class RestTester {

  @Autowired
  private MockMvc mockMvc;

  protected String getContentsFromResources(String path) throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(path).getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }

  protected Optional<FuseSession> loginAndGetSession(String filePath) throws Exception {
    String contents = getContentsFromResources(filePath);
    MvcResult mvcResult = mockMvc.perform(post("/user/login")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();

    String jsonString = mvcResult.getResponse().getContentAsString();
    GeneralResponse generalResponse = new ObjectMapper().readValue(jsonString, GeneralResponse.class);

    if (generalResponse.getStatus() != OK) {
      return Optional.empty();
    }

    FuseSession session = new ObjectMapper().convertValue(generalResponse.getData(), FuseSession.class);
    return Optional.of(session);
  }

  protected GeneralResponse makePostRequest(String sessionId, String filePath, String urlPath) throws Exception {
    String contents = getContentsFromResources(filePath);

    MvcResult mvcResult = mockMvc.perform(post(urlPath)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("SESSIONID", sessionId)
        .content(contents)).andReturn();

    String jsonString = mvcResult.getResponse().getContentAsString();

    return new ObjectMapper().readValue(jsonString, GeneralResponse.class);
  }
}
