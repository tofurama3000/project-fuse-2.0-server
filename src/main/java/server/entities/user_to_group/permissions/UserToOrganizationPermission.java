package server.entities.user_to_group.permissions;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.CREATE_PROJECT_IN_ORGANIZATION;
import static server.constants.RoleValue.OWNER;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationMemberRepository;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

public class UserToOrganizationPermission extends UserToGroupPermission<Organization> {

  @Setter
  private OrganizationMemberRepository repository;

  @Setter
  private Session session;

  public UserToOrganizationPermission(User user, Organization group) {
    super(user, group);
  }

  @Override
  protected boolean allowedToJoin() {
    return true;
  }

  @Override
  protected Session getSession() {
    return session;
  }

  @Override
  public Iterable<Integer> getRoles() {
    return repository.getRoles(group, user);
  }

  public boolean canCreateProjectsInOrganization() {
    Set<Integer> roles = new HashSet<>(repository.getRoles(group, user));
    return roles.contains(CREATE_PROJECT_IN_ORGANIZATION) || roles.contains(ADMIN) || roles.contains(OWNER);
  }
}
