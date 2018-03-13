package server.controllers.rest;

import static server.constants.RegistrationStatus.REGISTERED;
import static server.constants.RegistrationStatus.UNREGISTERED;
import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.DENIED;
import static server.controllers.rest.response.BaseResponse.Status.ERROR;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_REGISTRATION_KEY;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NO_INVITATION_FOUND;
import static server.controllers.rest.response.CannedResponse.NO_USER_FOUND;
import static server.utility.ApplicantUtil.filterApplicants;
import static server.utility.PagingUtil.getPagedResults;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import server.controllers.FuseSessionController;
import server.controllers.MembersOfGroupController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.BaseResponse.Status;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.email.StandardEmailSender;
import server.entities.dto.FuseSession;
import server.entities.dto.UploadFile;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplicant;
import server.entities.dto.group.project.ProjectInvitation;
import server.entities.dto.user.Friendship;
import server.entities.dto.user.UnregisteredUser;
import server.entities.dto.user.User;
import server.entities.dto.user.UserProfile;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserPermission;
import server.handlers.InvitationHandler;
import server.handlers.UserToGroupRelationshipHandler;
import server.repositories.FriendRepository;
import server.repositories.UnregisteredUserRepository;
import server.repositories.UserProfileRepository;
import server.repositories.UserRepository;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Controller
@Api(value = "User Endpoints")
@RequestMapping(value = "/users")
@SuppressWarnings("unused")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NotificationController notificationController;

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private FileController fileController;

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private ProjectApplicantRepository projectApplicantRepository;

  @Autowired
  private ProjectInvitationRepository projectInvitationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private UserProfileRepository userProfileRepository;

  @Autowired
  private OrganizationInvitationRepository organizationInvitationRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private OrganizationApplicantRepository organizationApplicantRepository;

  @Autowired
  private UnregisteredUserRepository unregisteredUserRepository;

  @Autowired
  private MembersOfGroupController membersOfGroupController;

  @Autowired
  private UserToGroupRelationshipHandler userToGroupRelationshipHandler;

  @Autowired
  private InvitationHandler invitationHandler;

  @Autowired
  private FriendRepository friendRepository;

  @Value("${fuse.fileUploadPath}")
  private String fileUploadPath;

  @Value("${fuse.requireRegistration}")
  private boolean requireRegistration;

  @Autowired
  private StandardEmailSender emailSender;

  private static IdGenerator generator = new AlternativeJdkIdGenerator();
  private Logger logger = LoggerFactory.getLogger(UserController.class);

  @ApiOperation(value = "Creates a new user",
      notes = "Must provide a name, password, and email")
  @PostMapping
  @ResponseBody
  public TypedResponse<User> addNewUser(
      @ApiParam(value = "The user information to create with", required = true)
      @RequestBody User user, HttpServletRequest request, HttpServletResponse response) {

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
      return new TypedResponse<>(response, errors);
    }

    assert user != null;

    if (requireRegistration) {
      user.setRegistrationStatus(UNREGISTERED);
    } else {
      user.setRegistrationStatus(REGISTERED);
    }

    if (user.getProfile() == null) {
      user.setProfile(new UserProfile());
    }
    User savedUser = userRepository.save(user);
    savedUser.indexAsync();
    Long id = savedUser.getId();

    if (requireRegistration) {
      String registrationKey = generator.generateId().toString();

      UnregisteredUser unregisteredUser = new UnregisteredUser();
      unregisteredUser.setUserId(id);
      unregisteredUser.setRegistrationKey(registrationKey);

      unregisteredUserRepository.save(unregisteredUser);

      emailSender.sendRegistrationEmail(user.getEmail(), registrationKey);
    }

    return new TypedResponse<>(response, OK, errors, savedUser);
  }

  @ApiIgnore
  @PostMapping(path = "/login")
  @ResponseBody
  public TypedResponse<FuseSession> login(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {

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
          return new TypedResponse<>(response, OK, null, fuseSessionController.createSession(dbUser));
        }
        errors.add("Invalid Credentials");
      }
    }

    return new TypedResponse<>(response, Status.DENIED, errors);
  }

  @ApiIgnore
  @PostMapping(path = "/logout")
  @ResponseBody
  public BaseResponse logout(HttpServletRequest request, HttpServletResponse response) {
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (session.isPresent()) {
      fuseSessionController.deleteSession(session.get());
      return new GeneralResponse(response, OK);
    } else {
      List<String> errors = new ArrayList<>();
      errors.add("No active session");
      return new GeneralResponse(response, ERROR, errors);
    }
  }

  @ApiOperation(value = "Get a user by their id")
  @GetMapping(path = "/{id}")
  @ResponseBody
  public TypedResponse<User> getUserbyID(
      @ApiParam(value = "The ID of the user")
      @PathVariable(value = "id") Long id,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    if (id == null) {
      errors.add(INVALID_FIELDS);
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    User byId = userRepository.findOne(id);
    if (byId == null) {
      errors.add(NO_USER_FOUND);
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    return new TypedResponse<>(response, OK, null, byId);
  }

  @ApiOperation(value = "Get a user by their email address")
  @GetMapping(path = "/get_by_email/{email}")
  @ResponseBody
  public TypedResponse<User> getUserbyEmail(
      @ApiParam("Email address of the user")
      @PathVariable(value = "email") String email, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    if (email == null) {
      errors.add(INVALID_FIELDS);
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    User byEmail = userRepository.findByEmail(email);
    if (byEmail == null) {
      errors.add(NO_USER_FOUND);
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    return new TypedResponse<>(response, OK, null, byEmail);
  }

  @CrossOrigin
  @ApiOperation(value = "Updates a user (must be logged in as that user)")
  @PutMapping(path = "/{id}")
  @ResponseBody
  public BaseResponse updateCurrentUser(
      @ApiParam(value = "ID of the user to update")
      @PathVariable long id, @RequestBody User userData, HttpServletRequest request, HttpServletResponse response) {
    //to Use profile for profile
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, Status.DENIED, errors);
    }

    User userToSave = session.get().getUser();

    if (id != userToSave.getId()) {
      // have this take care of misc updates by admins/moderators (e.g. flag user or revoke access)

      errors.add("Unable to edit user, permission denied");
      return new GeneralResponse(response, Status.DENIED, errors);
    }

    // Merging instead of direct copying ensures we're very clear about what can be edited, and it provides easy checks

    if (userData.getName() != null)
      userToSave.setName(userData.getName());

    if (userData.getEncoded_password() != null)
      userToSave.setEncoded_password(userData.getEncoded_password());

    if (userData.getProfile() != null) {
      if (userToSave.getProfile() == null) {
        userData.getProfile().setUser(userToSave);
        UserProfile profile = userProfileRepository.save(userData.getProfile());
        userToSave.setProfile(profile);
      } else {
        userToSave.setProfile(userToSave.getProfile().merge(userToSave.getProfile(), userData.getProfile()));
      }
    }
    userRepository.save(userToSave).indexAsync();
    return new GeneralResponse(response, Status.OK);
  }


  @GetMapping
  @ResponseBody
  @ApiOperation(value = "Get all users")
  public TypedResponse<List<User>> getAllUsers(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }
    return new TypedResponse<>(response, OK, null, Lists.newArrayList(userRepository.findAll()));
  }

  @GetMapping(path = "/{id}/joined/organizations")
  @ResponseBody
  @ApiOperation(value = "Get all organizations for the specified user")
  public TypedResponse<List<Organization>> getAllOrganizationsOfUser(
      @PathVariable Long id,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }
    User user = userRepository.findOne(id);

    List<Organization> organizationsUserIsPartOf = membersOfGroupController.getOrganizationsUserIsPartOf(user);

    return new TypedResponse<>(response, OK, null, getPagedResults(organizationsUserIsPartOf, page, pageSize));
  }


  @GetMapping(path = "/{id}/joined/projects")
  @ResponseBody
  @ApiOperation(value = "Get all projects for the specified user")
  public TypedResponse<List<Project>> getAllProjectsOfUser(
      @PathVariable Long id,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    User user = userRepository.findOne(id);

    List<Project> projectsUserIsPartOf = membersOfGroupController.getProjectsUserIsPartOf(user);

    return new TypedResponse<>(response, OK, null, getPagedResults(projectsUserIsPartOf, page, pageSize));
  }


  @GetMapping(path= "/{id}/friends")
  @ResponseBody
  @ApiOperation(value = "Get all friends for the specified user")
  public TypedResponse<List<Friendship>> getAllFriendsOfUser(
          @PathVariable Long id,
          HttpServletRequest request, HttpServletResponse response
  ) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    User user = userRepository.findOne(id);

    return new TypedResponse<>(response, OK, null, friendRepository.getFriends(user));
  }

  @GetMapping(path = "/{id}/projects/applications")
  @ResponseBody
  @ApiOperation(value = "Get all project applications for the user")
  public TypedResponse<List<ProjectApplicant>> getAllApplicationsOfUserProjects(
      @PathVariable Long id,
      @PathParam("status") String status,
      @PathParam("not_status") String not_status,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    if (!Objects.equals(session.get().getUser().getId(), id)) {
      errors.add("Access Denied");
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    List<ProjectApplicant> applicants;
    User user = session.get().getUser();

    if (status != null) {
      applicants = projectApplicantRepository.getApplicantsBySenderAndStatus(user, status);
    } else {
      applicants = projectApplicantRepository.getApplicantsBySender(user);
    }

    return new TypedResponse<>(response, OK, errors, filterApplicants(applicants, not_status == null ? "" : not_status));
  }

  @GetMapping(path = "/{id}/organizations/applications")
  @ResponseBody
  @ApiOperation(value = "Get all organization applications for the user")
  public TypedResponse<List<OrganizationApplicant>> getAllApplicationsOfUserOrganizations(
      @PathVariable Long id,
      @PathParam("status") String status,
      @PathParam("not_status") String not_status,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    if (!Objects.equals(session.get().getUser().getId(), id)) {
      errors.add("Access Denied");
      return new TypedResponse<>(response, Status.DENIED, errors);
    }

    List<OrganizationApplicant> applicants;
    User user = session.get().getUser();

    if (status != null) {
      applicants = organizationApplicantRepository.getApplicantsBySenderAndStatus(user, status);
    } else {
      applicants = organizationApplicantRepository.getApplicantsBySender(user);
    }

    return new TypedResponse<>(response, OK, errors, filterApplicants(applicants, not_status == null ? "" : not_status));
  }

  @GetMapping(path = "/register/{registrationKey}")
  @ResponseBody
  @ApiOperation(value = "Verify the user's email address")
  public TypedResponse<List<ProjectInvitation>> register(@PathVariable(value = "registrationKey") String registrationKey, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    User user = session.get().getUser();

    UnregisteredUser unregisteredUser = unregisteredUserRepository.findOne(user.getId());

    if (unregisteredUser == null) {
      errors.add(NO_USER_FOUND);
      return new TypedResponse<>(response, errors);
    }

    if (!unregisteredUser.getRegistrationKey().equals(registrationKey)) {
      errors.add(INVALID_REGISTRATION_KEY);
      return new TypedResponse<>(response, errors);
    }

    user.setRegistrationStatus(REGISTERED);
    userRepository.save(user);

    unregisteredUserRepository.delete(unregisteredUser);

    return new TypedResponse<>(response, OK, null,
        projectInvitationRepository.findByReceiver(user));
  }

  @GetMapping(path = "/incoming/invites/project")
  @ResponseBody
  @ApiOperation(value = "Get project invites for the current user")
  public TypedResponse<List<ProjectInvitation>> getProjectInvites(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    User user = session.get().getUser();

    return new TypedResponse<>(response, OK, null,
        projectInvitationRepository.findByReceiver(user));
  }

  @GetMapping(path = "/incoming/invites/organization")
  @ResponseBody
  @ApiOperation(value = "Get organizations invites for the current user")
  public TypedResponse<List<OrganizationInvitation>> getOrganizationInvites(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    User user = session.get().getUser();

    return new TypedResponse<>(response, OK, null,
        organizationInvitationRepository.findByReceiver(user));
  }

  @PostMapping(path = "/{action}/invite/project")
  @ResponseBody
  @ApiOperation(value = "Accept or decline project invite")
  public BaseResponse acceptOrDeclineProjectInvite(
      @ApiParam(value = "The action to perform", example = "accept,decline")
      @PathVariable(value = "action") String action,
      @ApiParam(value = "The project invitation information to accept")
      @RequestBody ProjectInvitation projectInvitation, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    ProjectInvitation savedInvitation = projectInvitationRepository.findOne(projectInvitation.getId());
    if (savedInvitation == null) {
      errors.add(NO_INVITATION_FOUND);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User user = session.get().getUser();
    if (!user.getId().equals(savedInvitation.getReceiver().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    Project project = savedInvitation.getGroup();
    if (action.equalsIgnoreCase("decline")) {
      return invitationHandler.declineProjectInvitation(response, savedInvitation, user, project);
    } else {
      return invitationHandler.acceptProjectInvitation(projectInvitation, response, savedInvitation, user, project);
    }
  }

  @PostMapping(path = "/upload/thumbnail")
  @ResponseBody
  @ApiOperation(value = "Uploads a new thumbnail",
      notes = "Max file size is 5MB")
  public TypedResponse<UploadFile> uploadThumbnail(@RequestParam("file") MultipartFile fileToUpload, HttpServletRequest request,
                                                   HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, GeneralResponse.Status.DENIED, errors);
    }
    String[] fileType = fileToUpload.getContentType().split("/");
    if (!fileType[0].equals("image")) {
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    UploadFile uploadFile;
    try {
      uploadFile = fileController.saveFile(fileToUpload, "avatar", session.get().getUser());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new TypedResponse<>(response, ERROR, e.getMessage());
    }

    User user = session.get().getUser();
    UserProfile profile = user.getProfile();
    if (profile == null) {
      profile = new UserProfile();
      user.setProfile(profile);
    }

    profile.setThumbnail_id(uploadFile.getId());
    userProfileRepository.save(user.getProfile());
    return new TypedResponse<>(response, OK, null, uploadFile);
  }


  @PostMapping(path = "/upload/background")
  @ResponseBody
  @ApiOperation(value = "Uploads a new background",
      notes = "Max file size is 5MB")
  public TypedResponse<UploadFile> uploadBackground(@RequestParam("file") MultipartFile fileToUpload,
                                                    HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, GeneralResponse.Status.DENIED, errors);
    }

    String fileType = fileToUpload.getContentType().split("/")[0];
    if (!fileType.equals("image")) {
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    UploadFile uploadFile;
    try {
      uploadFile = fileController.saveFile(fileToUpload, "background", session.get().getUser());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new TypedResponse<>(response, ERROR, errors);
    }

    User user = session.get().getUser();
    UserProfile profile = user.getProfile();
    if (profile == null) {
      profile = new UserProfile();
      user.setProfile(profile);
    }
    profile.setBackground_id(uploadFile.getId());
    userProfileRepository.save(user.getProfile());
    return new TypedResponse<>(response, OK, null, uploadFile);
  }

  @PostMapping(path = "/{action}/invite/organization")
  @ResponseBody
  @ApiOperation(value = "Accept or decline organization invite")
  public BaseResponse acceptOrganizationInvite(
      @ApiParam(value = "The action to perform", example = "accept,decline")
      @PathVariable(value = "action") String action,
      @ApiParam(value = "The organization invitation information to accept")
      @RequestBody OrganizationInvitation organizationInvitation, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    OrganizationInvitation savedInvitation = organizationInvitationRepository.findOne(organizationInvitation.getId());
    if (savedInvitation == null) {
      errors.add(NO_INVITATION_FOUND);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User user = session.get().getUser();
    if (!user.getId().equals(savedInvitation.getReceiver().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    Organization group = savedInvitation.getGroup();

    if (action.equalsIgnoreCase("decline")) {
      return invitationHandler.declineOrganizationInvitation(response, savedInvitation, user, group);
    } else {
      return invitationHandler.acceptOrganizationInvitation(organizationInvitation, response, savedInvitation, user, group);
    }
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