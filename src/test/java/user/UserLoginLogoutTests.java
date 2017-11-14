package user;


import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import com.fasterxml.jackson.databind.ObjectMapper;
import framework.RestTester;
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


@Transactional
public class UserLoginLogoutTests extends RestTester {

  @Autowired
  private MockMvc mockMvc;

  private User primaryUser;

  @Before
  public void setup() throws Exception {
    String contents = getContentsFromResources("addUser/addUser2");
    primaryUser = new ObjectMapper().readValue(contents, User.class);

    MvcResult mvcResult = mockMvc.perform(post("/user/add")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();

    String jsonString = mvcResult.getResponse().getContentAsString();
    System.out.println(jsonString);
  }

  @Test
  public void testLoginCreatesValidSession() throws Exception {

    String contents = getContentsFromResources("login/loginBodyRequest2");
    MvcResult mvcResult = mockMvc.perform(post("/user/login")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(contents)).andReturn();

    String jsonString = mvcResult.getResponse().getContentAsString();
    GeneralResponse generalResponse = new ObjectMapper().readValue(jsonString, GeneralResponse.class);

    assertTrue(generalResponse.getStatus() == OK);
    assertNull(generalResponse.getErrors());
    FuseSession session = new ObjectMapper().convertValue(generalResponse.getData(), FuseSession.class);

    assertEquals(primaryUser.getEmail(), session.getUser().getEmail());
  }
}
