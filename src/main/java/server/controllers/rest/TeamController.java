package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.SERVER_ERROR;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import static server.controllers.rest.response.GeneralResponse.Status.ERROR;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.SessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.Session;
import server.entities.dto.User;
import server.entities.dto.team.Team;
import server.repositories.TeamRespository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/team")
@Transactional
public class TeamController {

  private static Logger logger = LoggerFactory.getLogger(TeamController.class);

  @Autowired
  private SessionController sessionController;

  @Autowired
  private TeamRespository teamRespository;

  @Autowired
  private SessionFactory sessionFactory;

  @PostMapping(path = "/create")
  @ResponseBody
  public synchronized GeneralResponse createTeam(@RequestBody Team team, HttpServletRequest request,
                                                 HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<Session> session = sessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, errors);
    }

    if (team.getName() == null) {
      errors.add("team name can not be null");
      return new GeneralResponse(response, errors);
    }

    User user = session.get().getUser();
    List<Team> teams = getTeamsWith(user, team.getName());

    team.setOwner(user);

    if (teams.size() == 0) {
      teamRespository.save(team);
      return new GeneralResponse(response);
    } else {
      errors.add("team name already exists for user");
      return new GeneralResponse(response, errors);
    }

  }

  @PostMapping(path = "/delete")
  @ResponseBody
  public GeneralResponse deleteTeam(@RequestBody Team team, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();

    Optional<Session> session = sessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }

    if (team.getName() == null) {
      errors.add("team name can not be null");
      return new GeneralResponse(response, errors);
    }

    User user = session.get().getUser();
    List<Team> teams = getTeamsWith(user, team.getName());


    if (teams.size() == 0) {
      errors.add("Could not find team named: " + team.getName() + "owned by " + user.getName());
      return new GeneralResponse(response, errors);
    } else if (teams.size() != 1) {
      logger.error("Multiple teams found (" + teams.size() + ") for team name: " + team.getName()
          + " and owner id: " + user.getId());
      errors.add(SERVER_ERROR);
      return new GeneralResponse(response, ERROR, errors);
    } else {
      teamRespository.delete(teams.get(0));
      return new GeneralResponse(response);
    }

  }

  @SuppressWarnings("unchecked")
  private List<Team> getTeamsWith(User owner, String name) {
    Query query = sessionFactory.getCurrentSession()
        .createQuery("FROM Team t WHERE t.owner = :owner AND t.name = :name");

    query.setParameter("owner", owner);
    query.setParameter("name", name);

    return query.list();
  }
}
