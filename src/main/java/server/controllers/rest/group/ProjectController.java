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
import server.entities.dto.group.organization.OrganizationProfile;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectInvitation;
import server.entities.dto.group.project.ProjectMember;
import server.entities.dto.group.project.ProjectProfile;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupProfileRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.organization.OrganizationProfileRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectProfileRepository;
import server.repositories.group.project.ProjectRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/project")
@Transactional
@SuppressWarnings("unused")
public class ProjectController extends GroupController<Project, ProjectMember, ProjectProfile> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ProjectProfileRepository projectProfileRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private ProjectInvitationRepository projectInvitationRepository;

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  protected Project createGroup() {
    return new Project();
  }

  @Override
  protected GroupProfileRepository<ProjectProfile> getGroupProfileRepository() {
    return projectProfileRepository;
  }

  @Override
  protected GroupRepository<Project> getGroupRepository() {
    return projectRepository;
  }
  @Override
  protected GroupMemberRepository<Project, ProjectMember> getRelationshipRepository() {
    return projectMemberRepository;
  }

  @Override
  protected UserToGroupPermission getUserToGroupPermission(User user, Project group) {
    return permissionFactory.createUserToProjectPermission(user, group);
  }

  @Override
  protected void addRelationship(User user, Project group, int role) {
    ProjectMember member = new ProjectMember();

    member.setUser(user);
    member.setProject(group);
    member.setRoleId(role);

    projectMemberRepository.save(member);
  }

  @PostMapping(path = "/invite")
  @ResponseBody
  public GeneralResponse invite(@RequestBody ProjectInvitation projectInvitation,
                                HttpServletRequest request, HttpServletResponse response) {
    return generalInvite(projectInvitation, request, response);
  }

  @Override
  protected void saveInvitation(GroupInvitation<Project> invitation) {
    projectInvitationRepository.save(((ProjectInvitation) invitation));
  }
}
