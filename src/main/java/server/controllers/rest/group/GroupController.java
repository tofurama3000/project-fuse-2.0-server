package server.controllers.rest.group;

import static server.constants.InvitationStatus.PENDING;
import static server.constants.RoleValue.DEFAULT_USER;
import static server.constants.RoleValue.INVITED;
import static server.constants.RoleValue.OWNER;
import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_MSG;
import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_OR_INVITED;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.CannedResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.GroupMember;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupProfile;
import server.permissions.UserToGroupPermission;
import server.repositories.UserRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupProfileRepository;
import server.repositories.group.GroupRepository;
import server.utility.UserFindHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public abstract class GroupController<T extends Group, R extends GroupMember<T>, P extends GroupProfile> {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserFindHelper userFindHelper;

  @Autowired
  private  GroupProfileRepository groupProfileRepository;

  @Autowired
  private SessionFactory sessionFactory;

  private static Logger logger = LoggerFactory.getLogger(TeamController.class);

  @PostMapping(path = "/create")
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

  @PutMapping(path = "/{id}/update")
  @CrossOrigin
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


//    UserToGroupPermission permission = getUserToGroupPermission(user, groupToSave);
//    boolean canUpdate = permission.canUpdate();
//    if (!canUpdate) {
//      errors.add(INSUFFICIENT_PRIVELAGES);
//      return new GeneralResponse(response, DENIED, errors);
//    }


    // Merging instead of direct copying ensures we're very clear about what can be edited, and it provides easy checks

    if (groupData.getName() != null)
      groupToSave.setName(groupData.getName());

    if (groupData.getProfile() != null) {



      if(groupToSave.getProfile()==null) {

      GroupProfile save= getGroupProfileRepository().save(groupData.getProfile());
       // groupToSave.setProfile(save);
      }
      else groupToSave.getProfile().merge(groupToSave.getProfile(), groupData.getProfile());

    }
    getGroupRepository().save(groupToSave);
    return new GeneralResponse(response, GeneralResponse.Status.OK);
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
        removeRelationship(user, group, INVITED);
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
    UserToGroupPermission senderPermission = getUserToGroupPermission(sessionUser, groupInvitation.getGroup());

    if (!senderPermission.canInvite()) {
      errors.add(INSUFFICIENT_PRIVELAGES);
      return new GeneralResponse(response, DENIED, errors);
    }

    Optional<User> receiver = userFindHelper.findUserByEmailIfIdNotSet(groupInvitation.getReceiver());

    if (!receiver.isPresent() || !userRepository.exists(receiver.get().getId())) {
      errors.add(INVALID_FIELDS_FOR_CREATE);
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    UserToGroupPermission receiverPermission = getUserToGroupPermission(receiver.get(), groupInvitation.getGroup());

    if (!receiverPermission.canAcceptInvite()) {
      errors.add(ALREADY_JOINED_OR_INVITED);
      return new GeneralResponse(response, DENIED, errors);
    }

    groupInvitation.setStatus(PENDING);
    groupInvitation.setSender(sessionUser);
    saveInvitation(groupInvitation);
    addRelationship(receiver.get(), groupInvitation.getGroup(), INVITED);

    return new GeneralResponse(response);
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

    return new GeneralResponse(response, GeneralResponse.Status.OK, null, matching.get(0));
  }

  protected abstract T createGroup();

  @GetMapping(path = "/{id}/members")
  @ResponseBody
  public GeneralResponse getMembersOfGroup(@PathVariable(value = "id") T group, HttpServletRequest request, HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, getMembersOf(group));
  }

  @GetMapping(path = "/all")
  @ResponseBody
  protected GeneralResponse getAll(HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, getGroupRepository().findAll());
  }

  @GetMapping(path = "/{id}")
  @ResponseBody
  protected GeneralResponse getById(@PathVariable(value = "id") Long id, HttpServletResponse response) {
    Group res = getGroupRepository().findOne(id);
    if (res != null)
      return new GeneralResponse(response, GeneralResponse.Status.OK, null, res);
    List<String> errors = new LinkedList<String>();
    errors.add("Invalid ID! Object does not exist!");
    return new GeneralResponse(response, GeneralResponse.Status.BAD_DATA, errors);
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

  protected abstract GroupProfileRepository<P> getGroupProfileRepository();

  protected abstract GroupMemberRepository<T, R> getRelationshipRepository();

  protected abstract UserToGroupPermission getUserToGroupPermission(User user, T group);

  protected abstract void addRelationship(User user, T group, int role);

  protected abstract void saveInvitation(GroupInvitation<T> invitation);

  protected Session getSession() {
    return sessionFactory.openSession();
  }

  @SuppressWarnings("unchecked")
  private List<T> getGroupsWith(User owner, T group) {
    return toList(getGroupRepository().getGroups(owner, group.getName()));
  }

  private void removeRelationship(User user, T group, int role) {
    getRelationshipRepository().delete(group, user, role);
  }

  private List<User> getMembersOf(T group) {
    List<User> users = new ArrayList<>();
    Iterable<User> usersByGroup = getRelationshipRepository().getUsersByGroup(group);
    usersByGroup.forEach(users::add);
    return users;
  }

  private List<T> toList(Iterable<T> iterable) {
    List<T> list = new ArrayList<>();
    iterable.forEach(list::add);
    return list;
  }

}
