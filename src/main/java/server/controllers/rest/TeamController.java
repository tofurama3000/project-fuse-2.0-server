package server.controllers.rest;

import static server.constants.RoleValue.DEFAULT_USER;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.FuseSessionController;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.team.Team;
import server.entities.dto.team.TeamMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.TeamMemberRepository;
import server.repositories.TeamRepository;

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

  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }

}
