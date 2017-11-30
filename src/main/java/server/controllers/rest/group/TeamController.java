package server.controllers.rest.group;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.User;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.organization.OrganizationProfile;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamInvitation;
import server.entities.dto.group.team.TeamMember;
import server.entities.dto.group.team.TeamProfile;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupProfileRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.team.TeamInvitationRepository;
import server.repositories.group.team.TeamMemberRepository;
import server.repositories.group.team.TeamProfileRepository;
import server.repositories.group.team.TeamRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/team")
@Transactional
@SuppressWarnings("unused")
public class TeamController extends GroupController<Team, TeamMember> {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamProfileRepository teamProfileRepository;


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

}
