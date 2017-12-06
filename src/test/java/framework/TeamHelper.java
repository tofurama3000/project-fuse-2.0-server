package framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.group.team.Team;

import java.util.Optional;

@Service
public class TeamHelper {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RequestHelper requestHelper;

  public Optional<Team> getTeam(String email, String teamName) throws Exception {
    String url = "/team/find?email=" + email + "&name=" + teamName;
    GeneralResponse generalResponse = requestHelper.makeGetRequest(url);
    Team team = new ObjectMapper().convertValue(generalResponse.getData(), Team.class);
    return Optional.ofNullable(team);
  }
}
