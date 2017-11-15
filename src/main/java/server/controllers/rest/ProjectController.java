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
import server.entities.dto.project.Project;
import server.entities.dto.project.ProjectInvitation;
import server.entities.dto.project.ProjectMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.project.ProjectInvitationRepository;
import server.repositories.project.ProjectMemberRepository;
import server.repositories.project.ProjectRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "/project")
@Transactional
@SuppressWarnings("unused")
public class ProjectController extends GroupController<Project> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private ProjectRepository projectRepository;

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
  protected CrudRepository<Project, Long> getGroupRepository() {
    return projectRepository;
  }

  @Override
  protected CrudRepository<? extends UserToGroupRelationship, Long> getRelationshipRepository() {
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

  @Override
  protected Iterable<Project> getGroupsWith(User owner, Project group) {
    return null;
  }
}
