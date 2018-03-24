package server.controllers.rest.group;

import static server.controllers.rest.response.BaseResponse.Status.OK;
import io.swagger.annotations.Api;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.rest.response.BaseResponse;
import server.entities.PossibleError;
import server.entities.dto.group.GroupApplication;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectApplication;
import server.entities.dto.group.project.ProjectInvitation;
import server.entities.dto.group.project.ProjectMember;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.repositories.group.GroupApplicantRepository;
import server.repositories.group.GroupInvitationRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectInvitationRepository;
import server.repositories.group.project.ProjectMemberRepository;
import server.repositories.group.project.ProjectProfileRepository;
import server.repositories.group.project.ProjectRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/projects")
@Transactional
@Api("Projects")
@SuppressWarnings("unused")
public class ProjectController extends GroupController<Project, ProjectMember, ProjectInvitation> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private ProjectProfileRepository projectProfileRepository;

  @Autowired
  private ProjectApplicantRepository projecApplicantRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private ProjectInvitationRepository projectInvitationRepository;

  @Autowired
  private RelationshipFactory relationshipFactory;

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  protected Project createGroup() {
    return new Project();
  }

  @Override
  protected GroupRepository<Project> getGroupRepository() {
    return projectRepository;
  }

  @Override
  protected GroupApplicantRepository getGroupApplicantRepository() {
    return projecApplicantRepository;
  }

  @Override
  protected GroupProfile<Project> saveProfile(Project project) {
    return projectProfileRepository.save(project.getProfile());
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
  protected UserToGroupPermission<Project> getUserToGroupPermissionTyped(User user, Project group) {
    return permissionFactory.createUserToProjectPermission(user, group);
  }

  @Override
  protected void removeRelationship(User user, Project group, int role) {
    relationshipFactory.createUserToProjectRelationship(user, group).removeRelationship(role);
    List<User> list  = projectMemberRepository.getUsersByGroup(group);
    Set<User> set = new HashSet<>(list);
    group.setNum_members(new Long (set.size()));
    projectRepository.save(group);
    group.indexAsync();
  }

  @Override
  protected void addRelationship(User user, Project group, int role) {
    relationshipFactory.createUserToProjectRelationship(user, group).addRelationship(role);
    List<User> list  = projectMemberRepository.getUsersByGroup(group);
    Set<User> set = new HashSet<>(list);
    group.setNum_members(new Long (set.size()));
    projectRepository.save(group);
    group.indexAsync();
  }

  @Override
  protected GroupApplication<Project> getApplication() {
    return new ProjectApplication();
  }

  @Override
  protected ProjectInvitation getInvitation() {
    return new ProjectInvitation();
  }

  @Override
  protected GroupInvitationRepository<ProjectInvitation> getGroupInvitationRepository() {
    return projectInvitationRepository;
  }

  @Override
  protected PossibleError validateGroup(User user, Project group) {
    Organization parentOrganization = group.getOrganization();
    if (parentOrganization != null && parentOrganization.getId() == null) {
      group.setOrganization(null);
      parentOrganization = null;
    }
    if (parentOrganization != null) {
      UserToOrganizationPermission permission = permissionFactory.createUserToOrganizationPermission(user, parentOrganization);
      if (permission.canCreateProjectsInOrganization()) {
        return new PossibleError(OK);
      } else {
        List<String> errors = new ArrayList<>();
        errors.add("Do not have permission to add project to group");
        return new PossibleError(errors, BaseResponse.Status.DENIED);
      }
    } else {
      return new PossibleError(OK);
    }
  }

  @Override
  protected void saveInvitation(ProjectInvitation invitation) {
    projectInvitationRepository.save(invitation);
  }
}
