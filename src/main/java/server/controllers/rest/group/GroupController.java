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
import static server.utility.PagingUtil.getPagedResults;
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
import server.entities.MemberRelationship;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

  private Logger logger = LoggerFactory.getLogger(GroupController.class);

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

  private GeneralResponse generalApply(Long id, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    GroupApplicant<T> application = getApplication();
    T group = getGroupRepository().findOne(id);
    application.setGroup(group);

    switch (getUserToGroupPermission(session.get().getUser(), application.getGroup()).canJoin()) {
      case ALREADY_JOINED:
        errors.add(ALREADY_JOINED_MSG);
        return new GeneralResponse(response, ERROR, errors);
    }
    List<GroupApplicant> applicants = getGroupApplicantRepository().getApplicantsBySender(session.get().getUser());
    for (GroupApplicant applicant : applicants) {
      if (applicant.getGroup().getId().equals(id)) {
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
      logger.error(e.getMessage(), e);

      return new GeneralResponse(response, BaseResponse.Status.ERROR, "Could not send notification");
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
          logger.error(e.getMessage(), e);
        }
        return new GeneralResponse(response);
      case HAS_INVITE:
        addRelationship(user, group, DEFAULT_USER);
        removeRelationship(user, group, INVITED_TO_JOIN);
        try {
          notificationController.sendGroupNotificationToAdmins(group, user.getName() + " joined " + group.getName(),
              group.getGroupType() + "Invitation", group.getGroupType() + "Invitation:Accepted", group.getId());
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
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

  private GeneralResponse generalInvite(I groupInvitation, HttpServletRequest request, HttpServletResponse response) {
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
      logger.error(e.getMessage(), e);
    }
    return errors;
  }

  @PostMapping(path = "/{id}/invite/{applicant_id}/{type}")
  @ResponseBody
  public GeneralResponse invite(@PathVariable("id") Long id,
                                @PathVariable("applicant_id") Long applicantId,
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

  @CrossOrigin
  @ApiOperation(value = "Delete a interview slot", notes = "This endpoint is used to delete interview slot.")
  @PutMapping(path = "/{id}/interview_slots/delete")
  @ResponseBody
  public BaseResponse deleteInterviewSlots(
      @ApiParam("The ID of the interview to delete")
      @PathVariable("id") long id,
      HttpServletRequest request,
      HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    Interview interview = interviewRepository.findOne(id);
    if (interview == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BAD_DATA, errors);
    }
    interview.setDeleted(true);
    interviewRepository.save(interview);
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @ApiOperation(value = "Edit a interview slot", notes = "This endpoint is used to edit interview slot, user can only edit time.")
  @PutMapping(path = "/{id}/interview_slots/edit")
  @ResponseBody
  public BaseResponse editInterviewSlots(
      @ApiParam("The ID of the interview to delete")
      @PathVariable("id") long id,
      @ApiParam("Interview object")
      @RequestBody Interview interview,
      HttpServletRequest request,
      HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    Interview interviewTosave = interviewRepository.findOne(id);
    if (interviewTosave == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BAD_DATA, errors);
    }
    if (interview.getStart() != null && interview.getEnd() != null) {
      ZonedDateTime startZonedDateTime = ZonedDateTime.parse(interview.getStart());
      LocalDateTime startDateTime = startZonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
      ZonedDateTime endZonedDateTime = ZonedDateTime.parse(interview.getEnd());
      LocalDateTime endDateTime = endZonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
      if (endDateTime.isBefore(startDateTime)) {
        errors.add("Invalid time");
        return new GeneralResponse(response, DENIED, errors);
      }
      interview.setStart(interview.getStart());
      interview.setEnd(interview.getEnd());
    } else {
      if (interview.getStart() != null) {
        ZonedDateTime startZonedDateTime = ZonedDateTime.parse(interview.getStart());
        LocalDateTime startDateTime = startZonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        if (startDateTime.isAfter(interview.getEndDateTime())) {
          errors.add("Invalid time");
          return new GeneralResponse(response, DENIED, errors);
        }
        interview.setStart(interview.getStart());
      }

      if (interview.getEnd() != null) {
        ZonedDateTime endZonedDateTime = ZonedDateTime.parse(interview.getEnd());
        LocalDateTime endDateTime = endZonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        if (endDateTime.isBefore(interview.getStartDateTime())) {
          errors.add("Invalid time");
          return new GeneralResponse(response, DENIED, errors);
        }
        interview.setEnd(interview.getEnd());
      }
    }
    interviewRepository.save(interviewTosave);
    return new GeneralResponse(response, OK);
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

  @ApiOperation("Revokes access for a user")
  @PostMapping(path = "/{id}/members/{member_id}/kick")
  @ResponseBody
  public BaseResponse kickMemberAccess(
      @ApiParam("The id of the group to grant access for")
      @PathVariable(value = "id") Long id,
      @ApiParam("The id of the user to grant access to")
      @PathVariable(value = "member_id") Long memberId,
      HttpServletRequest request, HttpServletResponse response
  ) {
    return kickMember(id, memberId, ADMIN, response, request);
  }

  @ApiOperation("Grant admin access for a user")
  @PostMapping(path = "/{id}/members/{member_id}/grant/admin")
  @ResponseBody
  public BaseResponse grantAdminAccess(
      @ApiParam("The id of the group to grant access for")
      @PathVariable(value = "id") Long id,
      @ApiParam("The id of the user to grant access to")
      @PathVariable(value = "member_id") Long memberId,
      HttpServletRequest request, HttpServletResponse response
  ) {
    return grantAccessForMember(id, memberId, ADMIN, response, request);
  }

  @ApiOperation("Grant admin access for a user")
  @PostMapping(path = "/{id}/members/{member_id}/revoke/admin")
  @ResponseBody
  public BaseResponse revokeAdminAccess(
      @ApiParam("The id of the group to grant access for")
      @PathVariable(value = "id") Long id,
      @ApiParam("The id of the user to grant access to")
      @PathVariable(value = "member_id") Long memberId,
      HttpServletRequest request, HttpServletResponse response
  ) {
    return revokeAccessForMember(id, memberId, ADMIN, response, request);
  }

  @ApiOperation("Gets the members for the group")
  @GetMapping(path = "/{id}/members")
  @ResponseBody
  public TypedResponse<List<MemberRelationship>> getMembersOfGroup(
      @ApiParam("The id of the group to get the members for")
      @PathVariable(value = "id") Long id,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
      HttpServletRequest request, HttpServletResponse response) {

    T group = getGroupRepository().findOne(id);
    List<User> allMembers = new ArrayList<>(getMembersOf(group));

    Stream<MemberRelationship> stream = getPagedResults(allMembers, page, pageSize).stream().map(user -> {
      UserToGroupPermission permission = getUserToGroupPermission(user, group);
      MemberRelationship relationship = new MemberRelationship(user);
      relationship.setPermissions(permission);
      return relationship;
    });

    return new TypedResponse<>(response, BaseResponse.Status.OK, null, stream.collect(Collectors.toList()));
  }

  @GetMapping
  @ResponseBody
  @ApiOperation("Gets all of the groups of this type")
  protected TypedResponse<Iterable<T>> getAll(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }
    User user = session.get().getUser();

    return new TypedResponse<>(response, OK, null,
        StreamSupport.stream(getGroupRepository().findAll().spliterator(), false)
            .map(item -> this.setJoinPermissions(user, item))
            .collect(Collectors.toList())
    );
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
      res = setJoinPermissions(user, res);

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
        logger.error(e.getMessage(), e);
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
        logger.error(e.getMessage(), e);
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
  public TypedResponse<UploadFile> uploadThumbnail(@PathVariable(value = "id") Long id, @RequestParam("file")
      MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, GeneralResponse.Status.DENIED, errors);
    }

    String fileType = fileToUpload.getContentType().split("/")[0];
    if (!fileType.equals("image")) {
      return new TypedResponse<>(response, ERROR, errors);
    }
    UploadFile uploadFile;
    try {
      uploadFile = fileController.saveFile(fileToUpload, "avatar", session.get().getUser());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new TypedResponse<>(response, ERROR, errors);
    }

    T group = getGroupRepository().findOne(id);
    group.getProfile().setThumbnail_id(uploadFile.getId());
    getGroupApplicantRepository().save(group.getProfile());
    return new TypedResponse<>(response, OK, null, uploadFile);
  }

  @PostMapping(path = "/{id}/upload/background")
  @ResponseBody
  @ApiOperation(value = "Uploads a new background",
      notes = "Max file size is 128KB")
  public TypedResponse<UploadFile> uploadBackground(@PathVariable(value = "id") Long id, @RequestParam("file")
      MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) {
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
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }

    T group = getGroupRepository().findOne(id);
    group.getProfile().setBackground_id(uploadFile.getId());
    getGroupApplicantRepository().save(group.getProfile());
    return new TypedResponse<>(response, OK, null, uploadFile);
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
    long f_id = group.getProfile().getBackground_id();
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
      return new TypedResponse<>(response, GeneralResponse.Status.DENIED, errors);
    }
    T group = getGroupRepository().findOne(id);
    long f_id = group.getProfile().getThumbnail_id();
    if (f_id == 0) {
      errors.add(FILE_NOT_FOUND);
      return new TypedResponse<>(response, GeneralResponse.Status.DENIED, errors);
    }
    return new TypedResponse<>(response, OK, null, f_id);
  }

  private boolean validFieldsForCreate(T entity) {
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

  protected abstract UserToGroupPermission<T> getUserToGroupPermissionTyped(User user, T group);

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

  protected BaseResponse grantAccessForMember(Long id, Long memberId, int access, HttpServletResponse response, HttpServletRequest request) {
    T group = getGroupRepository().findOne(id);
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    User curUser = session.get().getUser();
    UserToGroupPermission curPermissions = getUserToGroupPermission(curUser, group);

    if (!curPermissions.canUpdate()) {
      errors.add("Permission denied");
      return new TypedResponse<>(response, DENIED, errors);
    }

    if (group == null) {
      errors.add("Group not found");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    Optional<User> userOptional = new ArrayList<>(getMembersOf(group))
        .stream()
        .filter(u -> u.getId().equals(memberId))
        .limit(1)
        .reduce((a, u) -> u);

    if (!userOptional.isPresent()) {
      errors.add("User is not part of the group!");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User user = userOptional.get();
    UserToGroupPermission permission = getUserToGroupPermission(user, group);

    if (permission.hasRole(access)) {
      return new GeneralResponse(response, OK);
    }
    addRelationship(user, group, access);
    return new GeneralResponse(response, OK);
  }

  BaseResponse revokeAccessForMember(Long id, Long memberId, int access, HttpServletResponse response, HttpServletRequest request) {
    T group = getGroupRepository().findOne(id);
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    User curUser = session.get().getUser();
    UserToGroupPermission curPermissions = getUserToGroupPermission(curUser, group);

    if (!curPermissions.canUpdate()) {
      errors.add("Permission denied");
      return new TypedResponse<>(response, DENIED, errors);
    }

    if (group == null) {
      errors.add("Group not found");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    Optional<User> userOptional = new ArrayList<>(getMembersOf(group))
        .stream()
        .filter(u -> u.getId().equals(memberId))
        .limit(1)
        .reduce((a, u) -> u);

    if (!userOptional.isPresent()) {
      errors.add("User is not part of the group!");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User user = userOptional.get();
    UserToGroupPermission permission = getUserToGroupPermission(user, group);

    if (!permission.hasRole(access)) {
      return new GeneralResponse(response, OK);
    }
    removeRelationship(user, group, access);
    return new GeneralResponse(response, OK);
  }

  private BaseResponse kickMember(Long id, Long memberId, int access, HttpServletResponse response, HttpServletRequest request) {
    T group = getGroupRepository().findOne(id);
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, DENIED, errors);
    }

    User curUser = session.get().getUser();
    UserToGroupPermission curPermissions = getUserToGroupPermission(curUser, group);

    if (!curPermissions.canUpdate()) {
      errors.add("Permission denied");
      return new TypedResponse<>(response, DENIED, errors);
    }

    if (group == null) {
      errors.add("Group not found");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    Optional<User> userOptional = new ArrayList<>(getMembersOf(group))
        .stream()
        .filter(u -> u.getId().equals(memberId))
        .limit(1)
        .reduce((a, u) -> u);

    if (!userOptional.isPresent()) {
      errors.add("User is not part of the group!");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    User user = userOptional.get();
    UserToGroupPermission<T> permission = getUserToGroupPermissionTyped(user, group);

    Iterable<Integer> roles = permission.getRoles();
    roles.forEach(integer -> removeRelationship(user, group, integer));

    return new GeneralResponse(response, OK);
  }


  private T setJoinPermissions(User user, T group) {
    UserToGroupPermission<T> permission = getUserToGroupPermissionTyped(user, group);
    genericSetJoinPermissions(user, group, permission);
    return group;
  }

  protected void genericSetJoinPermissions(User user, Group group, UserToGroupPermission permission) {
    group.setCanEdit(permission.canUpdate());
    switch (permission.canJoin()) {
      case OK:
        group.setCanJoin(true);
        group.setCanApply(false);
        break;
      case HAS_INVITE:
        group.setCanJoin(true);
        group.setCanApply(false);
        break;
      case NEED_INVITE:
        group.setCanJoin(false);
        group.setCanApply(true);
        break;
      case ALREADY_JOINED:
      case ERROR:
      default:
        group.setCanJoin(false);
        group.setCanApply(false);
    }
  }

  protected abstract GroupApplicant<T> getApplication();

  protected abstract I getInvitation();

  protected abstract GroupInvitationRepository<I> getGroupInvitationRepository();

  protected abstract PossibleError validateGroup(User user, T group);
}
