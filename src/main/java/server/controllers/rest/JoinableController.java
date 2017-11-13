package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_CREATE;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS_FOR_DELETE;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.SERVER_ERROR;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.ERROR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.SessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.Joinable;
import server.entities.dto.FuseSession;
import server.entities.dto.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
public abstract class JoinableController<T extends Joinable> {

  private final SessionController sessionController;
  private final CrudRepository<T, Long> repository;

  private static Logger logger = LoggerFactory.getLogger(TeamController.class);


  protected JoinableController(SessionController sessionController, CrudRepository<T, Long> repository) {
    this.sessionController = sessionController;
    this.repository = repository;
  }

  @PostMapping(path = "/create")
  @ResponseBody
  public synchronized GeneralResponse create(@RequestBody T entity, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = sessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (!validFieldsForCreate(entity)) {
      errors.add(INVALID_FIELDS_FOR_CREATE);
      return new GeneralResponse(response, errors);
    }

    User user = session.get().getUser();
    List<T> entities = getEntitiesWith(user, entity.getName());

    entity.setOwner(user);

    if (entities.size() == 0) {
      repository.save(entity);
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

    Optional<FuseSession> session = sessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (!validFieldsForDelete(entity)) {
      errors.add(INVALID_FIELDS_FOR_DELETE);
      return new GeneralResponse(response, errors);
    }

    User user = session.get().getUser();
    List<T> entities = getEntitiesWith(user, entity.getName());

    if (entities.size() == 0) {
      errors.add("Could not find entity named: '" + entity.getName() + "' owned by " + user.getName());
      return new GeneralResponse(response, errors);
    } else if (entities.size() != 1) {
      logger.error("Multiple teams found (" + entities.size() + ") for team name: " + entity.getName()
          + " and owner id: " + user.getId());
      errors.add(SERVER_ERROR);
      return new GeneralResponse(response, ERROR, errors);
    } else {
      repository.delete(entities.get(0));
      return new GeneralResponse(response);
    }
  }

  @GetMapping(path = "/all")
  @ResponseBody
  protected GeneralResponse getAll(HttpServletResponse response) {
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, repository.findAll());
  }

  protected abstract boolean validFieldsForCreate(T entity);

  protected abstract boolean validFieldsForDelete(T entity);

  protected abstract List<T> getEntitiesWith(User owner, String name);
}
