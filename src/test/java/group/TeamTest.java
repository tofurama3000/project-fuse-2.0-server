package group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import com.fasterxml.jackson.databind.ObjectMapper;
import framework.RestTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;

import java.util.Optional;

@Transactional
public class TeamTest extends RestTester {
  @Autowired
  private MockMvc mockMvc;

  private User user1;
  private User user2;

  @Before
  public void setup() throws Exception {
    String contents = getContentsFromResources("addUser/addUser1");
    user1 = new ObjectMapper().readValue(contents, User.class);

    mockMvc.perform(post("/user/add")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();

    contents = getContentsFromResources("addUser/addUser2");
    user2 = new ObjectMapper().readValue(contents, User.class);

    mockMvc.perform(post("/user/add")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();
  }

  @Test
  public void canOnlyJoinRestrictedTeamWithInvite() throws Exception {
    Optional<FuseSession> fuseSession1 = loginAndGetSession("login/loginUser1");
    assertTrue(fuseSession1.isPresent());

    createTeam1(fuseSession1.get().getSessionId());

    Optional<FuseSession> fuseSession2 = loginAndGetSession("login/loginUser2");
    assertTrue(fuseSession2.isPresent());

    assertFalse(tryJoinTeam1(fuseSession2.get().getSessionId()));

    inviteUser2ToTeam1(fuseSession1.get().getSessionId());

    assertTrue(tryJoinTeam1(fuseSession2.get().getSessionId()));

  }

  private void createTeam1(String sessionId) throws Exception {
    String contents = getContentsFromResources("team/createRestrictedTeam1");

    mockMvc.perform(post("/team/create")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("SESSIONID", sessionId)
        .content(contents)).andReturn();
  }

  private boolean tryJoinTeam1(String sessionId) throws Exception {
    GeneralResponse generalResponse = makePostRequest(sessionId, "join/joinTeam1", "/team/join");
    return generalResponse.getStatus() == OK;
  }

  private boolean inviteUser2ToTeam1(String sessionId) throws Exception {
    GeneralResponse generalResponse = makePostRequest(sessionId, "invite/inviteUser2ToTeam1", "/team/invite");
    return generalResponse.getStatus() == OK;
  }


}
