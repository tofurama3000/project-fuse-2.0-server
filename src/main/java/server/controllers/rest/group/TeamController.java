package server.controllers.rest.group;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.entities.PossibleError;
import server.entities.dto.user.User;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamApplicant;
import server.entities.dto.group.team.TeamInvitation;
import server.entities.dto.group.team.TeamMember;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.repositories.group.GroupApplicantRepository;
import server.repositories.group.GroupInvitationRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.team.TeamApplicantRepository;
import server.repositories.group.team.TeamInvitationRepository;
import server.repositories.group.team.TeamMemberRepository;
import server.repositories.group.team.TeamProfileRepository;
import server.repositories.group.team.TeamRepository;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping(value = "/teams")
@Transactional
@ApiIgnore
@SuppressWarnings("unused")
@Deprecated
public class TeamController extends GroupController<Team, TeamMember, TeamInvitation> {

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

  @Override
  protected GroupApplicant<Team> getApplication() {
    return new TeamApplicant();
  }

  @Override
  protected TeamInvitation getInvitation() {
    return new TeamInvitation();
  }

  @Override
  protected GroupInvitationRepository<TeamInvitation> getGroupInvitationRepository() {
    return teamInvitationRepository;
  }

  @Override
  protected PossibleError validateGroup(User user, Team group) {
    return null;
  }

  @Override
  protected void saveInvitation(TeamInvitation invitation) {
    teamInvitationRepository.save(invitation);
  }

}
