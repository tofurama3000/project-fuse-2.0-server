package server.controllers.rest;

import static server.constants.RoleValue.DEFAULT_USER;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.DENIED;

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
import server.constants.RoleValue;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.project.Project;
import server.entities.dto.project.ProjectMember;
import server.entities.dto.team.Team;
import server.entities.dto.team.TeamMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.ProjectMemberRepository;
import server.repositories.ProjectRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/project")
@Transactional
public class ProjectController extends GroupController<Project> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private FuseSessionController fuseSessionController;

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
  @PutMapping
  @ResponseBody
  public GeneralResponse updateGroup(@RequestBody Project project, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    User user = null;
    user.setId(session.get().getUser().getId());
    boolean  permission = getUserToGroupPermission(user, project).canUpdate();
    if(!permission){
      errors.add("Insufficient right");
      return new GeneralResponse(response, DENIED, errors);
    }
    projectRepository.save(project);
    return new GeneralResponse(response, GeneralResponse.Status.OK);
  }

  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }

}
