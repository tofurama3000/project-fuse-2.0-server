package group;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import com.fasterxml.jackson.databind.ObjectMapper;
import framework.JsonHelper;
import framework.RequestHelper;
import framework.RestTester;
import framework.SessionHelper;
import framework.TeamHelper;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.entities.dto.group.team.Team;
import server.repositories.UserRepository;
import server.repositories.group.team.TeamRepository;

import java.util.Optional;

@Transactional
public class TeamTest extends RestTester {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonHelper jsonHelper;

  @Autowired
  private SessionHelper sessionHelper;

  @Autowired
  private RequestHelper requestHelper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private TeamHelper teamHelper;

  private User user1;
  private User user2;

  @Before
  public void setup() throws Exception {
    String contents = requestHelper.getContentsFromResources("addUser/addUser1");
    user1 = new ObjectMapper().readValue(contents, User.class);

    mockMvc.perform(post("/user/add")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();

    contents = requestHelper.getContentsFromResources("addUser/addUser2");
    user2 = new ObjectMapper().readValue(contents, User.class);

    mockMvc.perform(post("/user/add")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();
  }

  @Test
  public void canOnlyJoinRestrictedTeamWithInvite() throws Exception {
    Optional<FuseSession> fuseSession1 = sessionHelper.loginAndGetSession("login/loginUser1");
    assertTrue(fuseSession1.isPresent());

    Optional<Team> team1 = createTeam1(fuseSession1.get().getSessionId());
    assertTrue(team1.isPresent());

    Optional<FuseSession> fuseSession2 = sessionHelper.loginAndGetSession("login/loginUser2");
    assertTrue(fuseSession2.isPresent());

    Optional<Team> team = teamHelper.getTeam(user1.getEmail(), team1.get().getName());
    assertFalse(tryJoinTeam1(fuseSession2.get().getSessionId(), team.get().getId()));

    inviteUser2ToTeam1(fuseSession1.get().getSessionId(), user2, team.get());

    assertTrue(tryJoinTeam1(fuseSession2.get().getSessionId(), team.get().getId()));

  }

  @Test
  public void updateTeam() throws Exception {
    Optional<FuseSession> fuseSession1 = sessionHelper.loginAndGetSession("login/loginUser1");
    assertTrue(fuseSession1.isPresent());

    Optional<Team> team1 = createTeam1(fuseSession1.get().getSessionId());
    assertTrue(team1.isPresent());

    Optional<FuseSession> fuseSession2 = sessionHelper.loginAndGetSession("login/loginUser2");
    assertTrue(fuseSession2.isPresent());

    Optional<Team> team = teamHelper.getTeam(user1.getEmail(), team1.get().getName());


    String putContents = "{\"id\":" + team.get().getId()  +"," + requestHelper.getContentsFromResources("updateGroup/updateGroup1");

    GeneralResponse generalResponse = requestHelper.makePutRequest(fuseSession1.get().getSessionId(), putContents, "/team/" + team.get().getId());
    TestCase.assertTrue(generalResponse.getStatus() == OK);
    assertNull(generalResponse.getErrors());
    assertEquals( teamRepository.findOne(team.get().getId()).getName(),"fusion");

    GeneralResponse generalResponse2 = requestHelper.makePutRequest(fuseSession2.get().getSessionId(), putContents, "/team/" + team.get().getId());
    TestCase.assertTrue(generalResponse2.getStatus() == DENIED);
  }
  private Optional<Team> createTeam1(String sessionId) throws Exception {
    GeneralResponse generalResponse = requestHelper.makePostRequestWithFile(sessionId,
        "team/createRestrictedTeam1", "/team/create");

    String contents = requestHelper.getContentsFromResources("team/createRestrictedTeam1");
    if (generalResponse.getStatus() == OK) {
      return Optional.of(new ObjectMapper().readValue(contents, Team.class));
    } else {
      return Optional.empty();
    }
  }

  private boolean tryJoinTeam1(String sessionId, Long teamId) throws Exception {
    String json = "{\"id\":" + teamId + "}";
    GeneralResponse generalResponse = requestHelper.makePostRequest(sessionId, json, "/team/join");
    return generalResponse.getStatus() == OK;
  }

  private boolean inviteUser2ToTeam1(String sessionId, User user, Team team) throws Exception {
    String invitation = jsonHelper.createInvitation(userRepository.findByEmail(user.getEmail()).getId(), team.getId());

    GeneralResponse generalResponse = requestHelper.makePostRequest(sessionId, invitation, "/team/invite");
    return generalResponse.getStatus() == OK;
  }


}
