package server.controllers.rest.group;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.organization.OrganizationProfile;
import server.entities.dto.group.team.*;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupProfileRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.team.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NO_USER_FOUND;
import static server.controllers.rest.response.GeneralResponse.Status.BAD_DATA;
import static server.controllers.rest.response.GeneralResponse.Status.OK;

@Controller
@RequestMapping(value = "/teams")
@Transactional
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

  //@Override
  //protected GroupProfileRepository<TeamProfile> getGroupProfileRepository() {
  //  return teamProfileRepository;
  //}

  @Override
  protected GroupRepository<Team> getGroupRepository() {
    return teamRepository;
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

  @GetMapping(path = "/{id}/applicants/{status}")
  @ResponseBody
  public GeneralResponse getApplicants(@PathVariable(value = "id") Long id,@PathVariable(value = "status") String status,
                                       HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }

    
    return new GeneralResponse(response, OK, null, teamApplicantRepository.getTeamApplicants(teamRepository.findOne(id),status));
  }
}
