package server.controllers.rest.group;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.User;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;
import server.entities.dto.group.team.TeamInvitation;
import server.entities.dto.group.team.TeamMember;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.repositories.group.GroupApplicantRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.team.TeamApplicantRepository;
import server.repositories.group.team.TeamInvitationRepository;
import server.repositories.group.team.TeamMemberRepository;
import server.repositories.group.team.TeamProfileRepository;
import server.repositories.group.team.TeamRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/teams")
@Transactional
@ApiIgnore
@SuppressWarnings("unused")
public class TeamController extends GroupController<Team, TeamMember> {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private TeamApplicantRepository teamApplicantRepository;

  @Autowired
  private TeamProfileRepository teamProfileRepository;


  @Autowired
  private TeamMemberRepository teamMemberRepository;

  @Autowired
  private TeamInvitationRepository teamInvitationRepository;

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private RelationshipFactory relationshipFactory;

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  public void addRelationship(User user, Team team, int role) {
    relationshipFactory.createUserToTeamRelationship(user, team).addRelationship(role);
  }

  @Override
  protected void removeRelationship(User user, Team group, int role) {
    relationshipFactory.createUserToTeamRelationship(user, group).removeRelationship(role);
  }

  @Override
  protected Team createGroup() {
    return new Team();
  }


  @Override
  protected GroupRepository<Team> getGroupRepository() {
    return teamRepository;
  }

  @Override
  protected GroupApplicantRepository getGroupApplicantRepository() {
    return teamApplicantRepository;
  }

  @Override
  protected GroupProfile<Team> saveProfile(Team team) {
    return teamProfileRepository.save(team.getProfile());
  }

  @Override
  protected GroupMemberRepository<Team, TeamMember> getRelationshipRepository() {
    return teamMemberRepository;
  }

  @Override
  protected UserToGroupPermission getUserToGroupPermission(User user, Team team) {
    return permissionFactory.createUserToTeamPermission(user, team);
  }

  @PostMapping(path = "/apply/{id}")
  @ResponseBody
  public GeneralResponse apply(@PathVariable(value = "id") Long id, @RequestBody TeamApplicant teamApplicant,
                               HttpServletRequest request, HttpServletResponse response) {
    return generalApply(id, teamApplicant, request, response);
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

}
