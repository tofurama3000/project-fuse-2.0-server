package framework;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;

import java.util.Optional;

@Service
public class SessionHelper {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RequestHelper requestHelper;

  public Optional<FuseSession> loginAndGetSession(String filePath) throws Exception {
    String contents = requestHelper.getContentsFromResources(filePath);
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
}
