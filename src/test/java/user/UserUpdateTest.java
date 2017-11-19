package user;

import com.fasterxml.jackson.databind.ObjectMapper;
import framework.JsonHelper;
import framework.RequestHelper;
import framework.RestTester;
import framework.SessionHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.repositories.UserRepository;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.OK;

public class UserUpdateTest extends RestTester {
    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JsonHelper jsonHelper;

    @Autowired
    private RequestHelper requestHelper;

    @Autowired
    private SessionHelper sessionHelper;

   // private User primaryUser;

    @Test

    public void updateUser() throws Exception {
        String contents = requestHelper.getContentsFromResources("addUser/addUser1");
        User primaryUser = new ObjectMapper().readValue(contents, User.class);

        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(contents)).andReturn();

        Optional<FuseSession> fuseSession1 = sessionHelper.loginAndGetSession("login/loginUser1");
        assertTrue(fuseSession1.isPresent());

        String putContents = requestHelper.getContentsFromResources("updateUser/updateUser1");
        GeneralResponse generalResponse = requestHelper.makePutRequest(fuseSession1.get().getSessionId(), putContents, "/user/update");

        assertTrue(generalResponse.getStatus() == OK);
        assertNull(generalResponse.getErrors());
        assertEquals( userRepository.findOne(fuseSession1.get().getUser().getId()).getName(),"test2");

    }
    @Test
    public void updateUser2() throws Exception {
        String contents = requestHelper.getContentsFromResources("addUser/addUser2");
        User primaryUser = new ObjectMapper().readValue(contents, User.class);

        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(contents)).andReturn();

        Optional<FuseSession> fuseSession1 = sessionHelper.loginAndGetSession("login/loginUser2");
        assertTrue(fuseSession1.isPresent());

        String putContents = requestHelper.getContentsFromResources("updateUser/updateUser2");
        GeneralResponse generalResponse = requestHelper.makePutRequest(fuseSession1.get().getSessionId(), putContents, "/user/update");

        assertTrue(generalResponse.getStatus() == DENIED);


    }
}
