package server.controllers.rest.group;

import static server.constants.Availability.AVAILABLE;
import static server.constants.InvitationStatus.PENDING;
import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.DEFAULT_USER;
import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.INVITED_TO_JOIN;
import static server.constants.RoleValue.OWNER;
import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.DENIED;
import static server.controllers.rest.response.BaseResponse.Status.ERROR;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_MSG;
import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_OR_INVITED;
import static server.controllers.rest.response.CannedResponse.FILE_NOT_FOUND;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INTERVIEW_NOT_AVAILABLE;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_CREATE;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;
import static server.controllers.rest.response.CannedResponse.SERVER_ERROR;
import static server.utility.RolesUtility.getRoleFromInvitationType;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import server.controllers.FuseSessionController;
import server.controllers.rest.FileController;
import server.controllers.rest.NotificationController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.CannedResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.PossibleError;
import server.entities.dto.FuseSession;
import server.entities.dto.UploadFile;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupMember;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.repositories.UserRepository;
import server.repositories.group.GroupApplicantRepository;
import server.repositories.group.GroupInvitationRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupProfileRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.InterviewRepository;
import server.utility.UserFindHelper;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class GroupController<T extends Group, R extends GroupMember<T>, I extends GroupInvitation<T>> {

  @Autowired
  protected FuseSessionController fuseSessionController;

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  private FileController fileController;

  @Autowired
  private NotificationController notificationController;

  @Autowired
  private InterviewRepository interviewRepository;

  @Autowired
  private UserFindHelper userFindHelper;

  @Autowired
  private GroupProfileRepository groupProfileRepository;

  @Autowired
  private SessionFactory sessionFactory;

  private static Logger logger = LoggerFactory.getLogger(TeamController.class);

  @PostMapping
  @ResponseBody
  @ApiOperation("Create a new entity")
  public synchronized TypedResponse<Group> create(
      @ApiParam("Entity information")
      @RequestBody T entity, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    if (!validFieldsForCreate(entity)) {
      errors.add(INVALID_FIELDS_FOR_CREATE);
      return new TypedResponse<>(response, errors);
    }

    if (entity.getProfile() == null) {
      errors.add("Missing profile information!");
      return new TypedResponse<>(response, errors);
    }

    User user = session.get().getUser();

    PossibleError possibleError = validateGroup(user, entity);

    if (possibleError.hasError()) {
      return new TypedResponse<>(response, possibleError.getStatus(), possibleError.getErrors());
    }

    List<T> entities = getGroupsWith(user, entity);
    entity.setOwner(user);

    if (entities.size() == 0) {
      Group savedEntity = getGroupRepository().save(entity);
      addRelationship(user, entity, OWNER);
      addRelationship(user, entity, ADMIN);
      savedEntity.indexAsync();
      return new TypedResponse<>(response, OK, null, savedEntity);
    } else {
      errors.add("entity name already exists for user");
      return new TypedResponse<>(response, errors);
    }
  }

  @DeleteMapping("/{id}")
  @ResponseBody
  @ApiOperation("Delete an entity")
  @ApiIgnore
  // TODO: Fix this; it doesn't work due to foreign key constraints with the profile entity
  public GeneralResponse delete(
      @ApiParam("ID of the entity to delete")
      @PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    Group g = getGroupRepository().findOne(id);
    if (g == null) {
      errors.add("Entity does not exist!");
      return new GeneralResponse(response, ERROR, errors);
    }

    User user = session.get().getUser();
    if (!Objects.equals(g.getOwner().getId(), user.getId())) {
      errors.add("Unable to delete entity, permission denied");
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    getGroupRepository().delete(id);
    return new GeneralResponse(response);
  }

  protected GeneralResponse generalApply(Long id, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    GroupApplicant<T> application = getApplication();
    T group = getGroupRepository().findOne(id);
    application.setGroup(group);

    // UserToTeamPermission permission = permissionFactory.createUserToTeamPermission(session.get().getUser(), applicant.getTeam());
    switch (getUserToGroupPermission(session.get().getUser(), application.getGroup()).canJoin()) {
      case ALREADY_JOINED:
        errors.add(ALREADY_JOINED_MSG);
        return new GeneralResponse(response, ERROR, errors);
    }
    List<GroupApplicant> list = getGroupApplicantRepository().getApplicantsBySender(session.get().getUser());
    for (GroupApplicant a : list) {
      if (a.getGroup().getId() == id) {
        errors.add("Already applied");
        return new GeneralResponse(response, ERROR, errors);
      }
    }
    application.setSender(session.get().getUser());
    application.setStatus(PENDING);
    if (group == null) {
      errors.add(NO_GROUP_FOUND);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    ZonedDateTime now = ZonedDateTime.now();
    application.setTime(now.toString());
    getGroupApplicantRepository().save(application);
    Map<String, Object> result = new HashMap<>();
    result.put("applied", true);
    try {
      notificationController.sendGroupNotificationToAdmins(group, session.get().getUser().getName() + " has applied to " + group.getName(),
          group.getGroupType() + "Applicant", group.getGroupType() + "Applicant", session.get().getUser().getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new GeneralResponse(response, BaseResponse.Status.OK, errors, result);
  }

  @CrossOrigin
  @PutMapping(path = "/{id}")
  @ApiOperation("Updates the specified entity")
  @ResponseBody
  public BaseResponse updateGroup(
      @ApiParam("The ID of the entity to update")
      @PathVariable(value = "id") long id,
      @ApiParam("The new data for the entity")
      @RequestBody T groupData, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User user = session.get().getUser();

    T groupToSave = getGroupRepository().findOne(id);

    UserToGroupPermission permission = getUserToGroupPermission(user, groupToSave);
    boolean canUpdate = permission.canUpdate();
    if (!canUpdate) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    // Merging instead of direct copying ensures we're very clear about what can be edited, and it provides easy checks
    if (groupData.getName() != null)
      groupToSave.setName(groupData.getName());

    if (groupData.getProfile() != null) {


      if (groupToSave.getProfile() == null) {
        groupData.getProfile().setGroup(groupToSave);
        GroupProfile profile = saveProfile(groupData);
        groupToSave.setProfile(profile);
      } else {
        groupToSave.setProfile(groupToSave.getProfile().merge(groupToSave.getProfile(), groupData.getProfile()));
      }
    }

    if (groupData.getRestrictionString() != null) {
      groupToSave.setRestriction(groupData.getRestrictionString());
    }
    getGroupRepository().save(groupToSave).indexAsync();
    return new GeneralResponse(response, OK);
  }

  @PostMapping(path = "/{id}/join")
  @ApiOperation("Join the group as the current user or applies if application is needed first")
  @ResponseBody
  protected synchronized GeneralResponse join(
      @ApiParam("The id of the group to join")
      @PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    T group = getGroupRepository().findOne(id);
    User user = session.get().getUser();


    switch (getUserToGroupPermission(user, group).canJoin()) {
      case OK:
        addRelationship(user, group, DEFAULT_USER);
        try {
          notificationController.sendGroupNotificationToAdmins(group, user.getName() + " joined " + group.getName(),
              group.getGroupType() + "Applicant", group.getGroupType() + "Applicant:Accepted", group.getId());
        } catch (Exception e) {
          e.printStackTrace();
        }
        return new GeneralResponse(response);
      case HAS_INVITE:
        addRelationship(user, group, DEFAULT_USER);
        removeRelationship(user, group, INVITED_TO_JOIN);
        try {
          notificationController.sendGroupNotificationToAdmins(group, user.getName() + " joined " + group.getName(),
              group.getGroupType() + "Invitation", group.getGroupType() + "Invitation:Accepted", group.getId());
        } catch (Exception e) {
          e.printStackTrace();
        }
        return new GeneralResponse(response);
      case NEED_INVITE:
        // Apply if an invite is needed
        return generalApply(id, request, response);
      case ALREADY_JOINED:
        errors.add(ALREADY_JOINED_MSG);
        return new GeneralResponse(response, ERROR, errors);
      case ERROR:
      default:
        errors.add(SERVER_ERROR);
        return new GeneralResponse(response, ERROR, errors);
    }
  }

  protected GeneralResponse generalInvite(I groupInvitation, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User sessionUser = session.get().getUser();
    T group = groupInvitation.getGroup();
    UserToGroupPermission senderPermission = getUserToGroupPermission(sessionUser, group);
    if (!senderPermission.canInvite()) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    Optional<User> receiver = userFindHelper.findUserByEmailIfIdNotSet(groupInvitation.getReceiver());

    if (!receiver.isPresent() || !userRepository.exists(receiver.get().getId())) {
      errors.add(INVALID_FIELDS_FOR_CREATE);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    UserToGroupPermission receiverPermission = getUserToGroupPermission(receiver.get(), group);

    Optional<Integer> role = getRoleFromInvitationType(groupInvitation.getType());

    if (!role.isPresent()) {
      errors.add("Unrecognized type");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    if (receiverPermission.isMember() || receiverPermission.hasRole(role.get())) {
      errors.add(ALREADY_JOINED_OR_INVITED);
      return new GeneralResponse(response, DENIED, errors);
    }

    errors = setInviteFields(groupInvitation, sessionUser, role.get(), receiver.get(), group);
    return new GeneralResponse(response, errors);
  }

  private List<String> setInviteFields(I groupInvitation, User sessionUser, Integer role, User receiver, T group) {
    List<String> errors = new ArrayList<>();

    groupInvitation.setStatus(PENDING);
    groupInvitation.setSender(sessionUser);
    switch (role) {
      case INVITED_TO_JOIN:
        addRelationship(receiver, group, INVITED_TO_JOIN);
        saveInvitation(groupInvitation);
        break;
      case INVITED_TO_INTERVIEW:
        LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        List<Interview> availableInterviewsAfterDate = interviewRepository.getAvailableInterviewsAfterDate(group.getId(), group.getGroupType(), currentDateTime);
        if (availableInterviewsAfterDate.size() == 0) {
          errors.add(INTERVIEW_NOT_AVAILABLE);
          return errors;
        }

        addRelationship(receiver, group, INVITED_TO_INTERVIEW);
        saveInvitation(groupInvitation);
        break;
    }

    try {
      notificationController.sendNotification(groupInvitation.getReceiver(), "You have been invited to join " + group.getName(), group.getGroupType() + "Invitation", group.getGroupType() + "Invitation:Invite", groupInvitation.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return errors;
  }

  @PostMapping(path = "/{id}/invite/{applicant_id}/{type}")
  @ResponseBody
  public GeneralResponse invite(@PathVariable("id") Long id,
                                @PathVariable("user_id") Long applicantId,
                                @PathVariable("type") String inviteType,
                                HttpServletRequest request, HttpServletResponse response) {
    I invite = getInvitation();
    List<String> errors = new ArrayList<>();
    invite.setGroup(getGroupRepository().findOne(id));
    GroupApplicantRepository applicantRespo = getGroupApplicantRepository();
    GroupApplicant<T> applicant = (GroupApplicant) applicantRespo.findOne(applicantId);
    if (applicant == null) {
      errors.add("Applicant not found");
      return new GeneralResponse(response, errors);
    }
    invite.setApplicant(applicant);
    invite.setReceiver(applicant.getSender());
    invite.setType(inviteType);
    return generalInvite(invite, request, response);
  }


  @ApiOperation(value = "Add a new interview slot", notes = "This creates a new interview slot that can be used when scheduling interviews.")
  @PostMapping(path = "/{id}/interview_slots/add")
  @ResponseBody
  public BaseResponse addInterviewSlots(
      @ApiParam("The ID of the group to add the slot to")
      @PathVariable("id") long id,
      @ApiParam("An array of interview slots to add")
      @RequestBody List<Interview> interviews, HttpServletRequest request,
      HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (!isValidInterviewSlots(interviews, session.get().getUser(), id)) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, errors);
    }

    T group = getGroupRepository().findOne(id);
    for (Interview interview : interviews) {
      interview.setGroupType(group.getGroupType());
      interview.setAvailability(AVAILABLE);
      interview.setGroupId(id);
    }

    interviewRepository.save(interviews);
    return new GeneralResponse(response, OK);
  }

  @ApiOperation("Returns the available interview slots")
  @GetMapping(path = "/{id}/interview_slots/available")
  @ResponseBody
  public TypedResponse<List<Interview>> getAvailableInterviews(
      @ApiParam("ID of the group to get the interview slots for")
      @PathVariable("id") long id, HttpServletRequest request, HttpServletResponse response) {
    Group group = getGroupRepository().findOne(id);
    LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    List<Interview> availableInterviewsAfterDate =
        interviewRepository.getAvailableInterviewsAfterDate(id, group.getGroupType(), currentDateTime);

    return new TypedResponse<>(response, OK, new ArrayList<>(), availableInterviewsAfterDate);
  }

  @ApiOperation("Find a group by the name and/or owner email")
  @GetMapping(path = "/find", params = {"name", "email"})
  @ResponseBody
  public TypedResponse<T> findByNameAndOwner(
      @ApiParam("Name of the group to get")
      @RequestParam(value = "name") String name,
      @ApiParam("Email address of the owner")
      @RequestParam(value = "email") String email,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    User user = new User();
    user.setEmail(email);
    Optional<User> userOptional = userFindHelper.findUserByEmailIfIdNotSet(user);
    if (!userOptional.isPresent() || name == null) {
      errors.add(INVALID_FIELDS);
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    T group = createGroup();
    group.setName(name);

    List<T> matching = getGroupsWith(userOptional.get(), group);
    if (matching.size() == 0) {
      errors.add(CannedResponse.NO_GROUP_FOUND);
      return new TypedResponse<>(response, errors);
    }

    return new TypedResponse<>(response, OK, null, matching.get(0));
  }

  protected abstract T createGroup();

  @ApiOperation("Gets the members for the group")
  @GetMapping(path = "/{id}/members")
  @ResponseBody
  public TypedResponse<List<User>> getMembersOfGroup(
      @ApiParam("The id of the group to get the members for")
      @PathVariable(value = "id") T group,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
      HttpServletRequest request, HttpServletResponse response) {

    List<User> list = new ArrayList<>(getMembersOf(group));
    List<User> returnList = new ArrayList<>();
    for (int i = page * pageSize; i < (page * pageSize) + pageSize; i++) {
      if (i >= list.size()) {
        break;
      }
      returnList.add(list.get(i));
    }

    return new TypedResponse<>(response, BaseResponse.Status.OK, null, returnList);
  }

  @GetMapping
  @ResponseBody
  @ApiOperation("Gets all of the groups of this type")
  protected TypedResponse<Iterable<T>> getAll(HttpServletResponse response) {
    return new TypedResponse<>(response, OK, null, getGroupRepository().findAll());
  }

  @GetMapping(path = "/{id}")
  @ApiOperation("Gets the group entity by id")
  @ResponseBody
  protected TypedResponse<T> getById(
      @ApiParam("ID of the gruop to get the id for")
      @PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    T res = getGroupRepository().findOne(id);
    if (res != null) {
      User user = session.get().getUser();
      T groupToFindRestriction = getGroupRepository().findOne(id);
      UserToGroupPermission permission = getUserToGroupPermission(user, groupToFindRestriction);
      res.setCanEdit(permission.canUpdate());

      return new TypedResponse<>(response, OK, null, res);
    }
    errors.add("Invalid ID! Object does not exist!");

    return new TypedResponse<>(response, BAD_DATA, errors);
  }

  @GetMapping(path = "/{id}/applicants/{status}")
  @ApiOperation("Get applicants by status")
  @ResponseBody
  public GeneralResponse getApplicants(@ApiParam("ID of entity")
                                       @PathVariable(value = "id")
                                           Long id,
                                       @ApiParam("Applicant status (one of 'accepted', 'declined', 'pending' 'interviewed', 'interview_scheduled')")
                                       @PathVariable(value = "status")
                                           String status,
                                       HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    if (GroupApplicant.ValidStatuses().indexOf(status) == -1) {
      errors.add("Invalid status!");
      return new GeneralResponse(response, BaseResponse.Status.BAD_DATA, errors);
    }

    UserToGroupPermission permission = getUserToGroupPermission(session.get().getUser(), getGroupRepository().findOne(id));
    boolean canUpdate = permission.canUpdate();
    if (!canUpdate) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    GroupApplicantRepository groupApplicantRepository = getGroupApplicantRepository();

    return new GeneralResponse(response, OK, null, groupApplicantRepository.getApplicants(getGroupRepository().findOne(id), status));
  }

  @CrossOrigin
  @PutMapping(path = "/{id}/applicants/{appId}/{status}")
  @ResponseBody
  public BaseResponse setApplicantsStatus(@ApiParam("ID of entity")
                                          @PathVariable(value = "id") Long id,
                                          @ApiParam("Applicant status (one of 'accepted', 'declined', 'pending' 'interviewed', 'interview_scheduled')")
                                          @PathVariable(value = "status") String status,
                                          @ApiParam("ID of application to update")
                                          @PathVariable(value = "appId") Long appId,
                                          HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    if (GroupApplicant.ValidStatuses().indexOf(status) == -1) {
      errors.add("Invalid status!");
      return new GeneralResponse(response, BaseResponse.Status.BAD_DATA, errors);
    }

    UserToGroupPermission permission = getUserToGroupPermission(session.get().getUser(), getGroupRepository().findOne(id));
    boolean canUpdate = permission.canUpdate();
    if (!canUpdate) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    GroupApplicantRepository groupApplicantRepository = getGroupApplicantRepository();
    GroupApplicant applicantToSave = (GroupApplicant) groupApplicantRepository.findOne(appId);
    if (applicantToSave.getStatus().equals(status)) {
      return new GeneralResponse(response, OK);
    }
    applicantToSave.setStatus(status);
    groupApplicantRepository.save(applicantToSave);

    ZonedDateTime now = ZonedDateTime.now();

    if (status.equals("declined")) {
      try {
        notificationController.sendNotification(applicantToSave.getSender(), applicantToSave.getGroup().getName() + "'s admin rejected your application",
            applicantToSave.getGroup().getGroupType() + "Applicant", applicantToSave.getGroup().getGroupType() + "Applicant:Declined", applicantToSave.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (!status.equals("interview_scheduled")) {
      List<Interview> interviews = interviewRepository.getAllByUserAndGroupTypeAndGroup(applicantToSave.getSender(), applicantToSave.getGroup().getGroupType(), applicantToSave.getGroup().getId());
      for (Interview interview : interviews) {
        interview.setUser(null);
        interview.setAvailability(AVAILABLE);
      }
      interviewRepository.save(interviews);
    } else {
      // Runs if status set to interview_scheduled
      T g = getGroupRepository().findOne(applicantToSave.getGroup().getId());
      I invite = getInvitation();
      invite.setGroup(g);
      invite.setReceiver(applicantToSave.getSender());
      invite.setType("interview");
      setInviteFields(invite, session.get().getUser(), INVITED_TO_INTERVIEW, applicantToSave.getSender(), g);
      invite.setStatus(PENDING);
      invite = getGroupInvitationRepository().save(invite);

      try {
        notificationController.sendNotification(applicantToSave.getSender(),
            "You have been invited to interview with " + applicantToSave.getGroup().getName() + "!",
            applicantToSave.getGroup().getGroupType() + "Invitation", applicantToSave.getGroup().getGroupType() + "Invitation:Invite",
            invite.getId()
        );
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (status.equals("invited")) {
      I invite = getInvitation();
      invite.setGroup(getGroupRepository().findOne(id));
      invite.setReceiver(userRepository.findOne(applicantToSave.getSender().getId()));
      invite.setType("join");

      Optional<Integer> role = getRoleFromInvitationType(invite.getType());

      if (!role.isPresent()) {
        errors.add("Unrecognized type");
        return new GeneralResponse(response, BAD_DATA, errors);
      }

      errors = setInviteFields(invite,
          session.get().getUser(),
          role.get(),
          applicantToSave.getSender(),
          getGroupRepository().findOne(applicantToSave.getGroup().getId()));
      if (errors.size() > 0) {
        return new GeneralResponse(response, ERROR, errors);
      }
    }

    return new GeneralResponse(response, OK);
  }

  @GetMapping(path = "/{id}/can_edit")
  @ApiOperation("Returns whether or not the current user can edit the group")
  @ResponseBody
  protected GeneralResponse canEdit(
      @ApiParam("The id of the group to check against")
      @PathVariable(value = "id") Long id, @RequestBody T groupData, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    User user = session.get().getUser();
    T groupToSave = getGroupRepository().findOne(id);
    UserToGroupPermission permission = getUserToGroupPermission(user, groupToSave);
    boolean canUpdate = permission.canUpdate();
    if (!canUpdate) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }
    return new GeneralResponse(response, BaseResponse.Status.OK);

  }

  @PostMapping(path = "/{id}/upload/thumbnail")
  @ResponseBody
  @ApiOperation(value = "Uploads a new thumbnail",
      notes = "Max file size is 128KB")
  public BaseResponse uploadThumbnail(@PathVariable(value = "id") Long id, @RequestParam("file") MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    String fileType = fileToUpload.getContentType().split("/")[0];
    if (!fileType.equals("image")) {
      return new GeneralResponse(response, BAD_DATA, errors);
    }
    TypedResponse<UploadFile> response1 = fileController.fileUpload(fileToUpload, request, response);
    if (response1.getStatus() == DENIED) {
      return new GeneralResponse(response, response1.getStatus(), response1.getErrors());
    }
    UploadFile uploadFile = (UploadFile) response1.getData();
    T group = getGroupRepository().findOne(id);
    group.getProfile().setThumbnail_id(uploadFile.getId());
    getGroupApplicantRepository().save(group.getProfile());
    return new GeneralResponse(response, OK, errors);
  }

  @PostMapping(path = "/{id}/upload/background")
  @ResponseBody
  @ApiOperation(value = "Uploads a new background",
      notes = "Max file size is 128KB")
  public BaseResponse uploadBackground(@PathVariable(value = "id") Long id, @RequestParam("file") MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    String fileType = fileToUpload.getContentType().split("/")[0];
    if (!fileType.equals("image")) {
      return new GeneralResponse(response, BAD_DATA, errors);
    }
    TypedResponse<UploadFile> response1 = fileController.fileUpload(fileToUpload, request, response);
    if (response1.getStatus() == DENIED) {
      return new GeneralResponse(response, response1.getStatus(), response1.getErrors());
    }
    UploadFile uploadFile = (UploadFile) response1.getData();
    T group = getGroupRepository().findOne(id);
    group.getProfile().setBackground_Id(uploadFile.getId());
    getGroupApplicantRepository().save(group.getProfile());
    return new GeneralResponse(response, OK, errors);
  }

  @GetMapping(path = "/{id}/download/background")
  @ResponseBody
  @ApiOperation(value = "Download a background file")
  public TypedResponse<Long> downloadBackground(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    T group = getGroupRepository().findOne(id);
    long f_id = group.getProfile().getBackground_Id();
    if (f_id == 0) {
      errors.add(FILE_NOT_FOUND);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    return new TypedResponse<>(response, OK, null, f_id);
  }

  @GetMapping(path = "/{id}/download/thumbnail")
  @ResponseBody
  @ApiOperation(value = "Download a background file")
  public TypedResponse<Long> downloadThumbnail(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    T group = getGroupRepository().findOne(id);
    long f_id = group.getProfile().getThumbnail_id();
    if (f_id == 0) {
      errors.add(FILE_NOT_FOUND);
      return new TypedResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    return new TypedResponse(response, OK, null, f_id);
  }

  protected boolean validFieldsForCreate(T entity) {
    return entity.getName() != null;
  }

  protected boolean validFieldsForDelete(T entity) {
    return entity.getName() != null;
  }

  protected abstract GroupRepository<T> getGroupRepository();

  protected abstract GroupApplicantRepository getGroupApplicantRepository();

  protected abstract GroupProfile saveProfile(T group);

  protected abstract GroupMemberRepository<T, R> getRelationshipRepository();

  protected abstract UserToGroupPermission getUserToGroupPermission(User user, T group);

  protected abstract void removeRelationship(User user, T group, int role);

  protected abstract void addRelationship(User user, T group, int role);

  protected abstract void saveInvitation(I invitation);

  protected Session getSession() {
    return sessionFactory.openSession();
  }

  @SuppressWarnings("unchecked")
  private List<T> getGroupsWith(User owner, T group) {
    return getGroupRepository().getGroups(owner, group.getName());
  }

  private Set<User> getMembersOf(T group) {
    Set<User> users = new HashSet<>();
    Iterable<User> usersByGroup = getRelationshipRepository().getUsersByGroup(group);
    usersByGroup.forEach(users::add);
    return users;
  }

  private boolean isValidInterviewSlots(List<Interview> interviews, User user, long groupId) {
    LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    if (interviews.size() <= 0) {
      return false;
    }

    T group = getGroupRepository().findOne(groupId);
    if (group == null) {
      return false;
    }

    UserToGroupPermission permission = getUserToGroupPermission(user, group);
    if (!permission.canUpdate()) {
      return false;
    }

    for (Interview interview : interviews) {
      if (interview.getStartDateTime() == null || interview.getEndDateTime() == null) {
        return false;
      }
      if (interview.getStartDateTime().isAfter(interview.getEndDateTime())) {
        return false;
      }
      if (interview.getStartDateTime().isBefore(currentDateTime)) {
        return false;
      }
    }

    return true;
  }

  protected abstract GroupApplicant<T> getApplication();

  protected abstract I getInvitation();

  protected abstract GroupInvitationRepository<I> getGroupInvitationRepository();

  protected abstract PossibleError validateGroup(User user, T group);
}
