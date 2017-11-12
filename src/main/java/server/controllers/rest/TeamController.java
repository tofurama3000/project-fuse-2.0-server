package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.SessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.User;
import server.entities.dto.team.Team;
import server.repositories.TeamRespository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/team")
public class TeamController {

  @Autowired
  private SessionController sessionController;

  @Autowired
  private TeamRespository teamRespository;

  @PostMapping(path = "/create")
  public @ResponseBody
  GeneralResponse createTeam(@RequestBody User user, @RequestBody Team team, HttpServletRequest request, HttpServletResponse response) {
    // should we take in user?
    List<String> errors = new ArrayList<>();
    if (sessionController.isSessionValid(user, request)) {
      errors.add(INVALID_SESSION);
    }

    if (team.getName() == null) {
      errors.add("team name can not be null");
    }

    team.setTeamOwnerId(user.getId());

    if (errors.size() == 0)
      teamRespository.save(team);

    return new GeneralResponse(response, errors);
  }

  @PostMapping(path = "/delete")
  public @ResponseBody
  GeneralResponse deleteTeam(@RequestBody User user, @RequestBody Team team, HttpServletRequest request, HttpServletResponse response) {
    // should we take in user?
    List<String> errors = new ArrayList<>();
    if (sessionController.isSessionValid(user, request)) {
      errors.add(INVALID_SESSION);
    }

    if (team.getName() == null) {
      errors.add("team name can not be null");
    }

    if (user.getId() != team.getTeamOwnerId()) {
      errors.add(INSUFFICIENT_PRIVELAGES);
    }

    if (errors.size() == 0) {
      teamRespository.delete(team);
      return new GeneralResponse(response);
    } else {
      return new GeneralResponse(response, DENIED, errors);
    }

  }
}
