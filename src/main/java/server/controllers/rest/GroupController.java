package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.ALREADY_JOINED_MSG;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_CREATE;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_DELETE;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NEED_INVITE_MSG;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;
import static server.controllers.rest.response.CannedResponse.SERVER_ERROR;
import static server.controllers.rest.response.GeneralResponse.Status.BAD_DATA;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.ERROR;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.Group;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.permissions.UserToGroupPermission;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
public abstract class GroupController<T extends Group> {

  private final FuseSessionController fuseSessionController;

  private static Logger logger = LoggerFactory.getLogger(TeamController.class);


  protected GroupController(FuseSessionController fuseSessionController) {
    this.fuseSessionController = fuseSessionController;
  }

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
    List<T> entities = getEntitiesWith(user, entity);

    entity.setOwner(user);

    if (entities.size() == 0) {
      getGroupRepository().save(entity);
      return new GeneralResponse(response);
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
    List<T> entities = getEntitiesWith(user, entity);

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

  @PostMapping(path = "/join")
  @ResponseBody
  protected synchronized GeneralResponse join(@RequestBody T group, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    User owner = group.getOwner();
    List<T> matching = getEntitiesWith(owner, group);
    if (matching.size() == 0) {
      errors.add(NO_GROUP_FOUND);
      return new GeneralResponse(response, BAD_DATA, errors);
    } else if (matching.size() != 1) {
      errors.add(SERVER_ERROR);
      return new GeneralResponse(response, ERROR, errors);
    }

    group = matching.get(0);

    User user = session.get().getUser();

    switch (getUserToGroupPermission(user, group).canJoin()) {
      case OK:
        addMember(user, group);
        return new GeneralResponse(response);
      case HAS_INVITE:
        addMember(user, group);
        // TODO Remove invite
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

  @GetMapping(path = "/{id}/members")
  @ResponseBody
  public GeneralResponse getMembersOfGroup(@PathVariable(value = "id") T group, HttpServletRequest request, HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null,  getMembersOf(group));
  }

  @GetMapping(path = "/all")
  @ResponseBody
  protected GeneralResponse getAll(HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, getGroupRepository().findAll());
  }

  protected boolean validFieldsForCreate(T entity) {
    return entity.getName() != null;
  }

  protected boolean validFieldsForDelete(T entity) {
    return entity.getName() != null;
  }

  protected abstract CrudRepository<T, Long> getGroupRepository();

  protected abstract CrudRepository<? extends UserToGroupRelationship, Long> getRelationshipRepository();

  protected abstract UserToGroupPermission getUserToGroupPermission(User user, T group);

  protected abstract void addMember(User user, T group);

  protected abstract Session getSession();

  @SuppressWarnings("unchecked")
  private List<T> getEntitiesWith(User owner, T group) {
    Query query = getSession()
        .createQuery("FROM " + group.getTableName() + " e WHERE e.owner = :owner AND e.name = :name");

    query.setParameter("owner", owner);
    query.setParameter("name", group.getName());

    return query.list();
  }

  @SuppressWarnings("unchecked")
  private List<T> getMembersOf(T group) {
    Query query = getSession()
        // 'group.getTableName().toLowerCase()' is kind of a hack, if run into issues make method to get this from subclass
        .createQuery("SELECT user FROM " + group.getRelationshipTableName() + " e WHERE e." + group.getTableName().toLowerCase() + "= :group");

    query.setParameter("group", group);

    return query.list();
  }
}
