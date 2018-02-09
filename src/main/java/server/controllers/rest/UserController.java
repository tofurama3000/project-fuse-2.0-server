package server.controllers.rest;

import static server.constants.Availability.NOT_AVAILABLE;
import static server.constants.InvitationStatus.ACCEPTED;
import static server.constants.InvitationStatus.DECLINED;
import static server.constants.InvitationStatus.PENDING;
import static server.constants.RegistrationStatus.REGISTERED;
import static server.constants.RegistrationStatus.UNREGISTERED;
import static server.constants.RoleValue.DEFAULT_USER;
import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.INVITED_TO_JOIN;
import static server.constants.RoleValue.TO_INTERVIEW;
import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.DENIED;
import static server.controllers.rest.response.BaseResponse.Status.ERROR;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.*;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.Application;
import server.controllers.FuseSessionController;
import server.controllers.MembersOfGroupController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.BaseResponse.Status;
import server.controllers.rest.response.TypedResponse;
import server.email.StandardEmailSender;
import server.entities.PossibleError;
import server.entities.dto.*;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplicant;
import server.entities.dto.group.project.ProjectInvitation;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;
import server.entities.dto.group.team.TeamInvitation;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserPermission;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.entities.user_to_group.permissions.UserToProjectPermission;
import server.entities.user_to_group.permissions.UserToTeamPermission;
import server.entities.user_to_group.permissions.results.JoinResult;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.entities.user_to_group.relationships.UserToGroupRelationship;
import server.entities.user_to_group.relationships.UserToOrganizationRelationship;
import server.entities.user_to_group.relationships.UserToProjectRelationship;
import server.entities.user_to_group.relationships.UserToTeamRelationship;
import server.repositories.FileRepository;
import server.repositories.UnregisteredUserRepository;
import server.repositories.UserProfileRepository;
import server.repositories.UserRepository;
import server.repositories.group.GroupApplicantRepository;
import server.repositories.group.InterviewRepository;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectRepository;
import server.repositories.group.team.TeamApplicantRepository;
import server.repositories.group.team.TeamInvitationRepository;
import server.repositories.group.team.TeamRepository;
import server.utility.RolesUtility;
import server.utility.StreamUtil;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
  private TeamInvitationRepository teamInvitationRepository;

  @Autowired
  private TeamApplicantRepository teamApplicantRepository;

  @Autowired
  private TeamRepository teamRepository;

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
  private InterviewRepository interviewRepository;

  @Autowired
  private RelationshipFactory relationshipFactory;

  @Autowired
  private MembersOfGroupController membersOfGroupController;

  @Value("${fuse.fileUploadPath}")
  private String fileUploadPath;

  @Value("${fuse.requireRegistration}")
  private boolean requireRegistration;

  @Autowired
  private StandardEmailSender emailSender;

  private static IdGenerator generator = new AlternativeJdkIdGenerator();

  @ApiOperation(value = "Creates a new user",
      notes = "Must provide a name, password, and email")
  @PostMapping
  @ResponseBody
  public GeneralResponse addNewUser(
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
      return new GeneralResponse(response, errors);
    }

    assert user != null;

    if (requireRegistration) {
      user.setRegistrationStatus(UNREGISTERED);
    } else {
      user.setRegistrationStatus(REGISTERED);
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

    return new GeneralResponse(response, OK, errors, savedUser);
  }

  @ApiIgnore
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

    return new GeneralResponse(response, Status.DENIED, errors);
  }

  @ApiIgnore
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
      return new GeneralResponse(response, Status.ERROR, errors);
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

  @GetMapping(path = "/{id}/joined/teams")
  @ResponseBody
  @ApiIgnore
  public GeneralResponse getAllTeamsOfUser(
      @PathVariable Long id,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, Status.DENIED, errors);
    }
    User user = userRepository.findOne(id);

    return new GeneralResponse(response, OK, null, membersOfGroupController.getTeamsUserIsPartOf(user));
  }

  @GetMapping(path = "/{id}/joined/organizations")
  @ResponseBody
  @ApiOperation(value = "Get all organizations for the specified user")
  public TypedResponse<List<Organization>> getAllOrganizationsOfUser(
      @PathVariable Long id,
      @ApiParam(value="The page of results to pull")
      @RequestParam(value = "page", required=false, defaultValue="0") int page,
      @ApiParam(value="The number of results per page")
      @RequestParam(value = "size", required=false, defaultValue="15") int pageSize,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, Status.DENIED, errors);
    }
    User user = userRepository.findOne(id);

    List<Organization> list =  membersOfGroupController.getOrganizationsUserIsPartOf(user);
    List<Organization> returnList = new ArrayList<Organization>();
    for(int i = page*pageSize; i<(page*pageSize)+pageSize;i++){
      if(i>=list.size()){
        break;
      }
      returnList.add(list.get(i));
    }

    return new TypedResponse<>(response, OK, null,returnList);
  }


  @GetMapping(path = "/{id}/joined/projects")
  @ResponseBody
  @ApiOperation(value = "Get all projects for the specified user")
  public TypedResponse<List<Project>> getAllProjectsOfUser(
      @PathVariable Long id,
      @ApiParam(value="The page of results to pull")
      @RequestParam(value = "page", required=false, defaultValue="0") int page,
      @ApiParam(value="The number of results per page")
      @RequestParam(value = "size", required=false, defaultValue="15") int pageSize,
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

    User user = userRepository.findOne(id);

    List<Project> list =  membersOfGroupController.getProjectsUserIsPartOf(user);
    List<Project> returnList = new ArrayList<Project>();
    for(int i = page*pageSize; i<(page*pageSize)+pageSize;i++){
      if(i>=list.size()){
        break;
      }
      returnList.add(list.get(i));
    }
    return new TypedResponse<>(response, OK, null, returnList);
  }

  @GetMapping(path = "/{id}/projects/applications")
  @ResponseBody
  @ApiOperation(value = "Get all project applications for the user")
  public TypedResponse<List<ProjectApplicant>> getAllApplicationsOfUserProjs(
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
  public TypedResponse<List<OrganizationApplicant>> getAllApplicationsOfUserOrgs(
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

  private <T extends GroupApplicant> List<T> filterApplicants(List<T> applicants, final String not_status) {
    return applicants.stream()
        .filter(projectApplicant -> projectApplicant.getStatus().compareToIgnoreCase(not_status) != 0)
        .sorted((o1, o2) -> {
          final Integer status1 = GroupApplicant.GetStatusOrder(o1.getStatus());
          final Integer status2 = GroupApplicant.GetStatusOrder(o2.getStatus());
          final int statusComp = status1.compareTo(status2);
          if (statusComp != 0) {
            return statusComp;
          }
          return o1.getGroup().getId().compareTo(o2.getGroup().getId());
        })
        .filter(StreamUtil.uniqueByFunction(projectApplicant -> projectApplicant.getGroup().getId()))
        .collect(Collectors.toList());
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


  @GetMapping(path = "/incoming/invites/team")
  @ResponseBody
  @ApiIgnore
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

  @PostMapping(path = "/accept/invite/team")
  @ResponseBody
  @ApiIgnore
  public GeneralResponse acceptTeamInvite(
      @ApiParam(value = "The team invitation information to accept")
      @RequestBody TeamInvitation teamInvitation, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    TeamInvitation savedInvitation = teamInvitationRepository.findOne(teamInvitation.getId());
    if (savedInvitation == null) {
      errors.add(NO_INVITATION_FOUND);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User user = session.get().getUser();
    if (!user.getId().equals(savedInvitation.getReceiver().getId())) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    Team group = savedInvitation.getGroup();
    UserToTeamPermission permission = permissionFactory.createUserToTeamPermission(user, group);

    UserToTeamRelationship userToTeamRelationship = relationshipFactory.createUserToTeamRelationship(user, group);

    savedInvitation.setInterview(teamInvitation.getInterview());
    PossibleError possibleError = addRelationshipsIfNotError(savedInvitation, permission, userToTeamRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      teamInvitationRepository.save(savedInvitation);
    }

    try {
      notificationController.sendGroupNotificationToAdmins(group, user.getName() + " has accepted invitation from " + group.getGroupType() + ": " + group.getName(),
          "TeamInvitation","TeamInvitation:Accepted",group.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }

  @PostMapping(path = "/{action}/invite/project")
  @ResponseBody
  @ApiOperation(value = "Accept or decline project invite")
  public BaseResponse acceptProjectInvite(
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
    Project group = savedInvitation.getGroup();

    if (action.equalsIgnoreCase("decline")){
      savedInvitation.setStatus(DECLINED);
      projectInvitationRepository.save(savedInvitation);

      try {
        notificationController.sendGroupNotificationToAdmins(group, user.getName() + " has declined invitation from " + group.getGroupType() + ": " + group.getName(),
            "ProjectInvitation","ProjectInvitation:Declined",group.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return new GeneralResponse(response);
    }

    UserToProjectPermission permission = permissionFactory.createUserToProjectPermission(user, group);

    UserToProjectRelationship userToTeamRelationship = relationshipFactory.createUserToProjectRelationship(user, group);

    if (projectInvitation.getInterview() != null) {
      savedInvitation.setInterview(interviewRepository.findOne(projectInvitation.getInterview().getId()));
    } else {
      savedInvitation.setInterview(null);
    }

    PossibleError possibleError = addRelationshipsIfNotError(savedInvitation, permission, userToTeamRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      projectInvitationRepository.save(savedInvitation);

      try {
        notificationController.sendGroupNotificationToAdmins(group, user.getName() + " has accepted invitation from " + group.getGroupType() + ": " + group.getName(),
            "ProjectInvitation","ProjectInvitation:Accepted",group.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }

  @PostMapping(path = "/upload/thumbnail")
  @ResponseBody
  @ApiOperation(value = "Uploads a new thumbnail",
      notes = "Max file size is 128KB")
  public BaseResponse uploadThumbnail(@RequestParam("file") MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    String fileType = fileToUpload.getContentType().split("/")[0];
    if(!fileType.equals("image")){
      return new GeneralResponse(response, BAD_DATA, errors);
    }
    TypedResponse<UploadFile> response1 = fileController.fileUpload(fileToUpload,request,response);
    if(response1.getStatus()==DENIED){
      return new GeneralResponse(response, response1.getStatus(), response1.getErrors());
    }
    UploadFile uploadFile = (UploadFile) response1.getData();
    User user = session.get().getUser();
    UserProfile profile =  user.getProfile();
    if(profile==null){
      profile = new UserProfile();
      user.setProfile(profile);
    }
    profile.setThumbnail_id(uploadFile.getId());
    userProfileRepository.save(user.getProfile());
    return new GeneralResponse(response, OK, errors);
  }


  @PostMapping(path = "/upload/background")
  @ResponseBody
  @ApiOperation(value = "Uploads a new background",
      notes = "Max file size is 128KB")
  public BaseResponse uploadBackground(@RequestParam("file") MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    String fileType = fileToUpload.getContentType().split("/")[0];
    if(!fileType.equals("image")){
      return new GeneralResponse(response, BAD_DATA, errors);
    }
    TypedResponse<UploadFile> response1 = fileController.fileUpload(fileToUpload,request,response);
    if(response1.getStatus()==DENIED){
      return new GeneralResponse(response, response1.getStatus(), response1.getErrors());
    }
    UploadFile uploadFile = (UploadFile) response1.getData();
    User user = session.get().getUser();
    UserProfile profile =  user.getProfile();
    if(profile==null){
      profile = new UserProfile();
      user.setProfile(profile);
    }
    profile.setBackground_Id(uploadFile.getId());
    userProfileRepository.save(user.getProfile());
    return new GeneralResponse(response, OK, errors);
  }

  @GetMapping(path = "/download/background")
  @ResponseBody
  @ApiOperation(value = "Download a background file")
  public TypedResponse<Long> downloadBackground( HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    User user = session.get().getUser();
    long id =user.getProfile().getBackground_Id();
    if(id==0){
    errors.add(FILE_NOT_FOUND);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    return new TypedResponse(response, OK,null,  id);
  }


  @GetMapping(path = "/download/thumbnail")
  @ResponseBody
  @ApiOperation(value = "Download a background file")
  public TypedResponse<Long> downloadThumbnail( HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    User user = session.get().getUser();

    long id = user.getProfile().getThumbnail_id();
    if(id==0){
      errors.add(FILE_NOT_FOUND);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    return new TypedResponse(response, OK,null, id);
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

    if (action.equalsIgnoreCase("decline")){
      savedInvitation.setStatus(DECLINED);
      organizationInvitationRepository.save(savedInvitation);
      try {
        notificationController.sendGroupNotificationToAdmins(group, user.getName() + " has declined invitation from " + group.getGroupType() + ": " + group.getName(),
            "OrganizationInvitation",  "OrganizationInvitation:Declined",group.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return new GeneralResponse(response);
    }
    UserToOrganizationPermission permission = permissionFactory.createUserToOrganizationPermission(user, group);

    UserToOrganizationRelationship userToTeamRelationship = relationshipFactory.createUserToOrganizationRelationship(user, group);

    savedInvitation.setInterview(organizationInvitation.getInterview());
    PossibleError possibleError = addRelationshipsIfNotError(savedInvitation, permission, userToTeamRelationship);

    if (!possibleError.hasError()) {
      savedInvitation.setStatus(ACCEPTED);
      organizationInvitationRepository.save(savedInvitation);
      try {
        notificationController.sendGroupNotificationToAdmins(group, user.getName() + " has accepted invitation from " + group.getGroupType() + ": " + group.getName()
                ,"OrganizationInvitation","OrganizationInvitation:Accepted",group.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return new GeneralResponse(response, possibleError.getStatus(), possibleError.getErrors());
  }


  private PossibleError addRelationshipsIfNotError(GroupInvitation invitation, UserToGroupPermission permission,
                                                   UserToGroupRelationship relationship) {

    List<String> errors = new ArrayList<>();

    User user = invitation.getReceiver();
    Optional<Integer> roleFromInvitationType = RolesUtility.getRoleFromInvitationType(invitation.getType());
    if (!roleFromInvitationType.isPresent()) {
      errors.add(INVALID_FIELDS);
      return new PossibleError(errors);
    }

    switch (roleFromInvitationType.get()) {
      case INVITED_TO_INTERVIEW:
        Interview interview = invitation.getInterview();
        if (interview == null) {
          errors.add(INVALID_FIELDS);
          return new PossibleError(errors);
        }
        Group group = invitation.getGroup();
        LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        List<Interview> availableInterviewsAfterDate = interviewRepository
            .getAvailableInterviewsAfterDate(group.getId(), group.getGroupType(), currentDateTime);

        long count = availableInterviewsAfterDate.stream().map(Interview::getId)
            .filter(id -> Objects.equals(id, interview.getId())).count();

        if (count < 1) {
          errors.add(NO_INTERVIEW_FOUND);
          return new PossibleError(errors);
        }

        if (!permission.hasRole(INVITED_TO_INTERVIEW)) {
          errors.add(INSUFFICIENT_PRIVELAGES);
          return new PossibleError(errors);
        }

        relationship.addRelationship(TO_INTERVIEW);
        relationship.removeRelationship(INVITED_TO_INTERVIEW);

        interview.setUser(user);
        interview.setAvailability(NOT_AVAILABLE);
        interviewRepository.save(interview);
        break;
      case INVITED_TO_JOIN:
        if (permission.canJoin() != JoinResult.HAS_INVITE) {
          errors.add(INSUFFICIENT_PRIVELAGES);
          return new PossibleError(errors);
        }
        relationship.addRelationship(DEFAULT_USER);
        relationship.removeRelationship(INVITED_TO_JOIN);
        break;
    }

    return new PossibleError(Status.OK);
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