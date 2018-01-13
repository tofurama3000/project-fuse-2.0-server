package server.controllers.rest.group;

import static server.constants.Availability.AVAILABLE;
import static server.constants.InvitationStatus.PENDING;
import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.DEFAULT_USER;
import static server.constants.RoleValue.INVITED_TO_INTERVIEW;
import static server.constants.RoleValue.INVITED_TO_JOIN;
import static server.constants.RoleValue.OWNER;
import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_MSG;
import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_OR_INVITED;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INTERVIEW_NOT_AVAILABLE;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_CREATE;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_DELETE;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NEED_INVITE_MSG;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;
import static server.controllers.rest.response.CannedResponse.SERVER_ERROR;
import static server.controllers.rest.response.GeneralResponse.Status.BAD_DATA;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.ERROR;
import static server.controllers.rest.response.GeneralResponse.Status.OK;
import static server.utility.RolesUtility.getRoleFromInvitationType;
import static server.controllers.rest.response.GeneralResponse.Status.BAD_DATA;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.ERROR;
import static server.controllers.rest.response.GeneralResponse.Status.OK;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.CannedResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.GroupMember;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.interview.Interview;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.dto.group.GroupProfile;
import server.repositories.UserRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupProfileRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.InterviewRepository;
import server.utility.UserFindHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@SuppressWarnings("unused")
public abstract class GroupController<T extends Group, R extends GroupMember<T>> {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private UserRepository userRepository;

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
  public synchronized GeneralResponse create(@RequestBody T entity, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (!validFieldsForCreate(entity)) {
      errors.add(INVALID_FIELDS_FOR_CREATE);
      return new GeneralResponse(response, errors);
    }

    User user = session.get().getUser();
    List<T> entities = getGroupsWith(user, entity);
    entity.setOwner(user);

    if (entities.size() == 0) {
      Group savedEntity = getGroupRepository().save(entity);
      addRelationship(user, entity, OWNER);
      addRelationship(user, entity, ADMIN);
      return new GeneralResponse(response, OK, null, savedEntity);
    } else {
      errors.add("entity name already exists for user");
      return new GeneralResponse(response, errors);
    }
  }

  @PostMapping(path = "/delete")
  @ResponseBody
  public GeneralResponse delete(@RequestBody T entity, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (!validFieldsForDelete(entity)) {
      errors.add(INVALID_FIELDS_FOR_DELETE);
      return new GeneralResponse(response, errors);
    }

    User user = session.get().getUser();
    List<T> entities = getGroupsWith(user, entity);

    if (entities.size() == 0) {
      errors.add("Could not find entity named: '" + entity.getName() + "' owned by " + user.getName());
      return new GeneralResponse(response, errors);
    } else if (entities.size() != 1) {
      logger.error("Multiple teams found (" + entities.size() + ") for team name: " + entity.getName()
          + " and owner id: " + user.getId());
      errors.add(SERVER_ERROR);
      return new GeneralResponse(response, ERROR, errors);
    } else {
      getGroupRepository().delete(entities.get(0));
      return new GeneralResponse(response);
    }
  }

  @CrossOrigin
  @PutMapping(path = "/{id}")
  @ResponseBody
  public GeneralResponse updateGroup(@PathVariable(value = "id") long id, @RequestBody T groupData, HttpServletRequest request, HttpServletResponse response) {

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
    getGroupRepository().save(groupToSave);
    return new GeneralResponse(response, OK);
  }

  @PostMapping(path = "/join")
  @ResponseBody
  protected synchronized GeneralResponse join(@RequestBody T group, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (group.getId() != null) {
      group = getGroupRepository().findOne(group.getId());
    } else {
      User owner = group.getOwner();
      List<T> matching = getGroupsWith(owner, group);
      if (matching.size() == 0) {
        errors.add(NO_GROUP_FOUND);
        return new GeneralResponse(response, BAD_DATA, errors);
      } else if (matching.size() != 1) {
        errors.add(SERVER_ERROR);
        return new GeneralResponse(response, ERROR, errors);
      }

      group = matching.get(0);
    }

    User user = session.get().getUser();

    switch (getUserToGroupPermission(user, group).canJoin()) {
      case OK:
        addRelationship(user, group, DEFAULT_USER);
        return new GeneralResponse(response);
      case HAS_INVITE:
        addRelationship(user, group, DEFAULT_USER);
        removeRelationship(user, group, INVITED_TO_JOIN);
        return new GeneralResponse(response);
      case NEED_INVITE:
        errors.add(NEED_INVITE_MSG);
        return new GeneralResponse(response, DENIED, errors);
      case ALREADY_JOINED:
        errors.add(ALREADY_JOINED_MSG);
        return new GeneralResponse(response, ERROR, errors);
      case ERROR:
      default:
        errors.add(SERVER_ERROR);
        return new GeneralResponse(response, ERROR, errors);
    }
  }

  protected GeneralResponse generalInvite(GroupInvitation<T> groupInvitation, HttpServletRequest request, HttpServletResponse response) {
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

    groupInvitation.setStatus(PENDING);
    groupInvitation.setSender(sessionUser);
    switch (role.get()) {
      case INVITED_TO_JOIN:
        addRelationship(receiver.get(), group, INVITED_TO_JOIN);
        saveInvitation(groupInvitation);
        break;
      case INVITED_TO_INTERVIEW:
        LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        List<Interview> availableInterviewsAfterDate = interviewRepository.getAvailableInterviewsAfterDate(group.getId(), group.getGroupType(), currentDateTime);
        if (availableInterviewsAfterDate.size() == 0) {
          errors.add(INTERVIEW_NOT_AVAILABLE);
          return new GeneralResponse(response, BAD_DATA, errors);
        }

        addRelationship(receiver.get(), group, INVITED_TO_INTERVIEW);
        saveInvitation(groupInvitation);
        break;
    }

    return new GeneralResponse(response);
  }

  @PostMapping(path = "/{id}/interview_slots/add")
  @ResponseBody
  public GeneralResponse addInterviewSlots(@PathVariable("id") long id, @RequestBody List<Interview> interviews, HttpServletRequest request,
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

  @GetMapping(path = "/{id}/interview_slots/available")
  @ResponseBody
  public GeneralResponse getAvailableInterviews(@PathVariable("id") long id, HttpServletRequest request, HttpServletResponse response) {
    Group group = getGroupRepository().findOne(id);
    LocalDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();

    List<Interview> availableInterviewsAfterDate =
        interviewRepository.getAvailableInterviewsAfterDate(id, group.getGroupType(), currentDateTime);

    return new GeneralResponse(response, OK, new ArrayList<>(), availableInterviewsAfterDate);
  }

  @GetMapping(path = "/find", params = {"name", "email"})
  @ResponseBody
  public GeneralResponse findByNameAndOwner(@RequestParam(value = "name") String name, @RequestParam(value = "email") String email,
                                            HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    User user = new User();
    user.setEmail(email);
    Optional<User> userOptional = userFindHelper.findUserByEmailIfIdNotSet(user);
    if (!userOptional.isPresent() || name == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    T group = createGroup();
    group.setName(name);

    List<T> matching = getGroupsWith(userOptional.get(), group);
    if (matching.size() == 0) {
      errors.add(CannedResponse.NO_GROUP_FOUND);
      return new GeneralResponse(response, errors);
    }

    return new GeneralResponse(response, OK, null, matching.get(0));
  }

  protected abstract T createGroup();

  @GetMapping(path = "/{id}/members")
  @ResponseBody
  public GeneralResponse getMembersOfGroup(@PathVariable(value = "id") T group, HttpServletRequest request, HttpServletResponse response) {
    return new GeneralResponse(response, OK, null, getMembersOf(group));
  }

  @GetMapping
  @ResponseBody
  protected GeneralResponse getAll(HttpServletResponse response) {
    return new GeneralResponse(response, OK, null, getGroupRepository().findAll());
  }

  @GetMapping(path = "/{id}")
  @ResponseBody
  protected GeneralResponse getById(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    Group res = getGroupRepository().findOne(id);
    if (res != null){
      User user = session.get().getUser();
      T groupToSave = getGroupRepository().findOne(id);
      UserToGroupPermission permission = getUserToGroupPermission(user, groupToSave);
      res.setCanEdit(permission.canUpdate());

      return new GeneralResponse(response, OK, null, res);
    }
    errors.add("Invalid ID! Object does not exist!");

    return new GeneralResponse(response, BAD_DATA, errors);
  }


  @GetMapping(path = "/{id}/can_edit")
  @ResponseBody
  protected GeneralResponse canEdit(@PathVariable(value = "id") Long id, @RequestBody T groupData, HttpServletRequest request, HttpServletResponse response) {
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
    return new GeneralResponse(response, GeneralResponse.Status.OK);

  }

  protected boolean validFieldsForCreate(T entity) {
    return entity.getName() != null;
  }

  protected boolean validFieldsForDelete(T entity) {
    return entity.getName() != null;
  }

  protected abstract GroupRepository<T> getGroupRepository();

  protected abstract GroupProfile saveProfile(T group);

  protected abstract GroupMemberRepository<T, R> getRelationshipRepository();

  protected abstract UserToGroupPermission getUserToGroupPermission(User user, T group);

  protected abstract void removeRelationship(User user, T group, int role);

  protected abstract void addRelationship(User user, T group, int role);

  protected abstract void saveInvitation(GroupInvitation<T> invitation);

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

}
