package server.permissions;

import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import server.entities.dto.User;
import server.entities.dto.organization.Organization;
import server.repositories.OrganizationMemberRepository;

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

}
