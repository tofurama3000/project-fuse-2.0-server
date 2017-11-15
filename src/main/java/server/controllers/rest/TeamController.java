package server.controllers.rest;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.GroupInvitation;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.team.Team;
import server.entities.dto.team.TeamInvitation;
import server.entities.dto.team.TeamMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.team.TeamInvitationRepository;
import server.repositories.team.TeamMemberRepository;
import server.repositories.team.TeamRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "/team")
@Transactional
@SuppressWarnings("unused")
public class TeamController extends GroupController<Team> {

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private TeamMemberRepository teamMemberRepository;

  @Autowired
  private TeamInvitationRepository teamInvitationRepository;

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  protected Team createGroup() {
    return new Team();
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
  protected void addRelationship(User user, Team team, int role) {
    TeamMember member = new TeamMember();
    member.setUser(user);
    member.setTeam(team);
    member.setRoleId(role);

    teamMemberRepository.save(member);
  }

  @PostMapping(path = "/invite")
  @ResponseBody
  public GeneralResponse invite(@RequestBody TeamInvitation teamInvitation,
                                HttpServletRequest request, HttpServletResponse response) {
    return generalInvite(teamInvitation, request, response);
  }

  @Override
  protected void saveInvitation(GroupInvitation<Team> invitation) {
    teamInvitationRepository.save(((TeamInvitation) invitation));
  }

  @Override
  protected Iterable<Team> getGroupsWith(User owner, Team group) {
    return teamRepository.getTeams(owner, group.getName());
  }
}
