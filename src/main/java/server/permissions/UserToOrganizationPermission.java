package server.permissions;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.User;
import server.entities.dto.group.organization.Organization;
import server.repositories.group.organization.OrganizationMemberRepository;

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

}
