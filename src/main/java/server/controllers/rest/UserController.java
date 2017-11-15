package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.permissions.PermissionFactory;
import server.permissions.UserPermission;
import server.repositories.UserRepository;
import server.repositories.organization.OrganizationInvitationRepository;
import server.repositories.project.ProjectInvitationRepository;
import server.repositories.team.TeamInvitationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/user")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private TeamInvitationRepository teamInvitationRepository;

  @Autowired
  private ProjectInvitationRepository projectInvitationRepository;

  @Autowired
  private OrganizationInvitationRepository organizationInvitationRepository;

  @PostMapping(path = "/add")
  @ResponseBody
  public GeneralResponse addNewUser(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();

    if (user != null) {
      if (user.getName() == null)
        errors.add("Missing Name");
      if (user.getEncoded_password() == null)
        errors.add("Missing Password");
      if (user.getEmail() == null)
        errors.add("Missing Email");
      if (errors.size() == 0 && userRepository.findByEmail(user.getEmail()) != null)
        errors.add("Username already exists!");
    } else
      errors.add("No request body found");

    if (errors.size() == 0)
      userRepository.save(user);

    return new GeneralResponse(response, errors);
  }

  @PostMapping(path = "/login")
  @ResponseBody
  public GeneralResponse login(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {

    logoutIfLoggedIn(user, request);

    List<String> errors = new ArrayList<>();
    if (user == null) {
      errors.add("Invalid Credentials");
    } else {
      User dbUser = userRepository.findByEmail(user.getEmail());

      if (dbUser == null) {
        errors.add("Invalid Credentials");
      } else {
        user.setEncoded_password(dbUser.getEncoded_password());

        if (user.checkPassword()) {
          return new GeneralResponse(response, GeneralResponse.Status.OK, null, fuseSessionController.createSession(dbUser));
        }
        errors.add("Invalid Credentials");
      }
    }

    return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
  }

  @PostMapping(path = "/logout")
  @ResponseBody
  public GeneralResponse logout(HttpServletRequest request, HttpServletResponse response) {
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (session.isPresent()) {
      fuseSessionController.deleteSession(session.get());
      return new GeneralResponse(response, GeneralResponse.Status.OK);
    } else {
      List<String> errors = new ArrayList<>();
      errors.add("No active session");
      return new GeneralResponse(response, GeneralResponse.Status.ERROR, errors);
    }
  }

  @GetMapping(path = "/{id}")
  @ResponseBody
  public GeneralResponse getUserbyID(@PathVariable(value = "id") long id, HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, userRepository.findOne(id));
  }

  @GetMapping(path = "/all")
  @ResponseBody
  public GeneralResponse getAllUsers(HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, userRepository.findAll());
  }

  @GetMapping(path = "/incoming/invites/project")
  @ResponseBody
  public GeneralResponse getInvites(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User user = session.get().getUser();

    return new GeneralResponse(response, GeneralResponse.Status.OK, null,
        projectInvitationRepository.findByReceiver(user));
  }

  private boolean logoutIfLoggedIn(User user, HttpServletRequest request) {
    UserPermission userPermission = permissionFactory.createUserPermission(user);
    if (userPermission.isLoggedIn(request)) {
      Optional<FuseSession> session = fuseSessionController.getSession(request);
      session.ifPresent(s -> fuseSessionController.deleteSession(s));
      return true;
    } else {
      return false;
    }
  }
}
