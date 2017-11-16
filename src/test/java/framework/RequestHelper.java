package framework;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import server.controllers.rest.response.GeneralResponse;

import java.io.File;
import java.nio.file.Files;

@Service
public class RequestHelper {

  @Autowired
  private MockMvc mockMvc;

  public String getContentsFromResources(String path) throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(path).getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }

  public GeneralResponse makePostRequestWithFile(String sessionId, String filePath, String urlPath) throws Exception {
    String contents = getContentsFromResources(filePath);

    return makePostRequest(sessionId, contents, urlPath);
  }

  public GeneralResponse makePostRequest(String sessionId, String contents, String urlPath) throws Exception {
    MvcResult mvcResult = mockMvc.perform(post(urlPath)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("SESSIONID", sessionId)
        .content(contents)).andReturn();

    String jsonString = mvcResult.getResponse().getContentAsString();

    return new ObjectMapper().readValue(jsonString, GeneralResponse.class);
  }

  public GeneralResponse makeGetRequest(String urlPath) throws Exception {

    MvcResult mvcResult = mockMvc.perform(get(urlPath)).andReturn();
    String jsonString = mvcResult.getResponse().getContentAsString();

    return new ObjectMapper().readValue(jsonString, GeneralResponse.class);
  }
}
