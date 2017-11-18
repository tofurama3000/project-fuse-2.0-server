package server.controllers.rest;

import static server.constants.RegistrationStatus.REGISTERED;
import static server.constants.RegistrationStatus.UNREGISTERED;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_REGISTRATION_KEY;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NO_USER_FOUND;
import static server.controllers.rest.response.GeneralResponse.Status.BAD_DATA;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.email.StandardEmailSender;
import server.entities.dto.FuseSession;
import server.entities.dto.UnregisteredUser;
import server.entities.dto.User;
import server.permissions.PermissionFactory;
import server.permissions.UserPermission;
import server.repositories.UnregisteredUserRepository;
import server.repositories.UserRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.team.TeamInvitationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;

@Controller
@RequestMapping(value = "/user")
@SuppressWarnings("unused")
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

  @Autowired
  private UnregisteredUserRepository unregisteredUserRepository;

  @Value("${fuse.requireRegistration}")
  private boolean requireRegistration;

  @Autowired
  private StandardEmailSender emailSender;

  private static IdGenerator generator = new AlternativeJdkIdGenerator();


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
    } else {
      errors.add("No request body found");
    }

    if (errors.size() != 0) {
      return new GeneralResponse(response, errors);
    }

    assert user != null;

    if (requireRegistration) {
      user.setRegistrationStatus(UNREGISTERED);
    } else {
      user.setRegistrationStatus(REGISTERED);
    }

    userRepository.save(user);
    Long id = userRepository.findByEmail(user.getEmail()).getId();

    if (requireRegistration) {
      String registrationKey = generator.generateId().toString();

      UnregisteredUser unregisteredUser = new UnregisteredUser();
      unregisteredUser.setUserId(id);
      unregisteredUser.setRegistrationKey(registrationKey);

      unregisteredUserRepository.save(unregisteredUser);

      emailSender.sendRegistrationEmail(user.getEmail(), registrationKey);
    }

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
          return new GeneralResponse(response, OK, null, fuseSessionController.createSession(dbUser));
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
      return new GeneralResponse(response, OK);
    } else {
      List<String> errors = new ArrayList<>();
      errors.add("No active session");
      return new GeneralResponse(response, GeneralResponse.Status.ERROR, errors);
    }
  }

  @GetMapping(path = "/{id}")
  @ResponseBody
  public GeneralResponse getUserbyID(@PathVariable(value = "id") Long id, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();

    if (id == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User byId = userRepository.findOne(id);
    if (byId == null) {
      errors.add(NO_USER_FOUND);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    return new GeneralResponse(response, OK, null, byId);
  }

  @GetMapping(path = "/{email}")
  @ResponseBody
  public GeneralResponse getUserbyEmail(@PathVariable(value = "email") String email, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();

    if (email == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User byEmail = userRepository.findByEmail(email);
    if (byEmail == null) {
      errors.add(NO_USER_FOUND);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    return new GeneralResponse(response, OK, null, byEmail);
  }

  @PutMapping
  @ResponseBody
  public GeneralResponse updateUser(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    user.setId(session.get().getUser().getId());
    userRepository.save(user);
    return new GeneralResponse(response, GeneralResponse.Status.OK);
  }

  @GetMapping(path = "/all")
  @ResponseBody
  public GeneralResponse getAllUsers(HttpServletResponse response) {
    return new GeneralResponse(response, OK, null, userRepository.findAll());
  }

  @GetMapping(path = "/register/{registrationKey}")
  @ResponseBody
  public GeneralResponse register(@PathVariable(value = "registrationKey") String registrationKey, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User user = session.get().getUser();

    UnregisteredUser unregisteredUser = unregisteredUserRepository.findOne(user.getId());

    if (unregisteredUser == null) {
      errors.add(NO_USER_FOUND);
      return new GeneralResponse(response, errors);
    }

    if (!unregisteredUser.getRegistrationKey().equals(registrationKey)) {
      errors.add(INVALID_REGISTRATION_KEY);
      return new GeneralResponse(response, errors);
    }

    user.setRegistrationStatus(REGISTERED);
    userRepository.save(user);

    unregisteredUserRepository.delete(unregisteredUser);

    return new GeneralResponse(response, OK, null,
        projectInvitationRepository.findByReceiver(user));
  }

  @GetMapping(path = "/incoming/invites/project")
  @ResponseBody
  public GeneralResponse getProjectInvites(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User user = session.get().getUser();

    return new GeneralResponse(response, OK, null,
        projectInvitationRepository.findByReceiver(user));
  }

  @GetMapping(path = "/incoming/invites/organization")
  @ResponseBody
  public GeneralResponse getOrganizationInvites(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User user = session.get().getUser();

    return new GeneralResponse(response, OK, null,
        organizationInvitationRepository.findByReceiver(user));
  }

  @GetMapping(path = "/incoming/invites/team")
  @ResponseBody
  public GeneralResponse getTeamInvites(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User user = session.get().getUser();

    return new GeneralResponse(response, OK, null,
        teamInvitationRepository.findByReceiver(user));
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
