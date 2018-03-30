package server.entities.user_to_group.permissions;

import lombok.Setter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.project.ProjectMemberRepository;

public class UserToProjectPermission extends UserToGroupPermission<Project> {

  @Setter
  private ProjectMemberRepository repository;

  @Setter
  private UserToOrganizationPermission userToOrganizationPermission;

  @Setter
  private Session session;

  public UserToProjectPermission(User user, Project group) {
    super(user, group);
  }

  @Override
  protected boolean allowedToJoin() {
    return userToOrganizationPermission == null || userToOrganizationPermission.isMember();
  }

  @Override
  protected Session getSession() {
    return session;
  }

  @Override
  public Iterable<Integer> getRoles() {
    return repository.getRoles(group, user);
  }
}
