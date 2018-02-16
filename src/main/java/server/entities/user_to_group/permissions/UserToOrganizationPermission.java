package server.entities.user_to_group.permissions;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.CREATE_PROJECT_IN_ORGANIZATION;
import static server.constants.RoleValue.OWNER;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationMemberRepository;

import java.util.HashSet;

public class UserToOrganizationPermission extends UserToGroupPermission<Organization> {

  @Setter
  private OrganizationMemberRepository repository;

  @Setter
  private Session session;

  public UserToOrganizationPermission(User user, Organization group) {
    super(user, group);
  }

  @Override
  protected Session getSession() {
    return session;
  }

  @Override
  protected String getGroupFieldName() {
    return "organization";
  }

  @Override
  protected Iterable<Integer> getRoles() {
    return repository.getRoles(group, user);
  }

  public boolean canCreateProjectsInOrganization() {
    HashSet<Integer> roles = new HashSet<>(repository.getRoles(group, user));
    return roles.contains(CREATE_PROJECT_IN_ORGANIZATION) || roles.contains(ADMIN) || roles.contains(OWNER);
  }
}
