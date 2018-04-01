package server.entities.user_to_group.permissions;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.controllers.FuseSessionController;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.project.ProjectApplicantRepository;
import server.repositories.group.project.ProjectMemberRepository;

@Service
@Transactional
public class PermissionFactory {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private ProjectApplicantRepository projectApplicantRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private OrganizationApplicantRepository organizationApplicantRepository;

  public UserPermission createUserPermission(User user) {
    UserPermission permission = new UserPermission(user);
    permission.setFuseSessionController(fuseSessionController);
    return permission;
  }

  public UserToOrganizationPermission createUserToOrganizationPermission(User user, Organization organization) {
    UserToOrganizationPermission permission = new UserToOrganizationPermission(user, organization);
    permission.setSession(sessionFactory.getCurrentSession());
    permission.setRepository(organizationMemberRepository);
    permission.setOrganizationApplicantRepository(organizationApplicantRepository);
    return permission;
  }

  public UserToProjectPermission createUserToProjectPermission(User user, Project project) {
    UserToProjectPermission permission = new UserToProjectPermission(user, project);
    permission.setSession(sessionFactory.getCurrentSession());
    permission.setRepository(projectMemberRepository);
    if (project.getOrganization() != null) {
      permission.setUserToOrganizationPermission(createUserToOrganizationPermission(user, project.getOrganization()));
    }
    return permission;
  }
}
