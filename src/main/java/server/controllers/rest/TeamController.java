package server.controllers.rest;

import static server.constants.RoleValue.DEFAULT_USER;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;

import server.controllers.FuseSessionController;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.team.Team;
import server.entities.dto.team.TeamMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.TeamMemberRepository;
import server.repositories.TeamRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/team")
@Transactional
public class TeamController extends GroupController<Team> {

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private TeamMemberRepository teamMemberRepository;

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  public TeamController(FuseSessionController fuseSessionController) {
    super(fuseSessionController);
  }

  @Override
  protected CrudRepository<Team, Long> getGroupRepository() {
    return teamRepository;
  }

  @Override
  protected CrudRepository<? extends UserToGroupRelationship, Long> getRelationshipRepository() {
    return teamMemberRepository;
  }

  @Override
  protected UserToGroupPermission getUserToGroupPermission(User user, Team team) {
    return permissionFactory.createUserToTeamPermission(user, team);
  }

  @Override
  protected void addMember(User user, Team team) {
    TeamMember member = new TeamMember();
    member.setUser(user);
    member.setTeam(team);
    member.setRoleId(DEFAULT_USER);

    teamMemberRepository.save(member);
  }


  @PutMapping
  @ResponseBody
  public GeneralResponse updateTeam(@RequestBody Team team, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    User user = null;
    user.setId(session.get().getUser().getId());

    //check permission;
    teamRepository.save(team);
    return new GeneralResponse(response, GeneralResponse.Status.OK);
  }
  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }

}
