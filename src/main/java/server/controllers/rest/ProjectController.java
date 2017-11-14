package server.controllers.rest;

import static server.constants.RoleValue.DEFAULT_USER;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.constants.RoleValue;
import server.controllers.FuseSessionController;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.project.Project;
import server.entities.dto.project.ProjectMember;
import server.entities.dto.team.TeamMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.ProjectMemberRepository;
import server.repositories.ProjectRepository;

@Controller
@RequestMapping(value = "/project")
@Transactional
public class ProjectController extends GroupController<Project> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  public ProjectController(FuseSessionController fuseSessionController) {
    super(fuseSessionController);
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
  protected void addMember(User user, Project group) {
    ProjectMember member = new ProjectMember();

    member.setUser(user);
    member.setProject(group);
    member.setRoleId(DEFAULT_USER);

    projectMemberRepository.save(member);
  }

  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }

}
