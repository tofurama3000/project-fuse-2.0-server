package server.controllers.rest;

import static server.constants.RoleValue.DEFAULT_USER;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.FuseSessionController;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.organization.Organization;
import server.entities.dto.organization.OrganizationMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.permissions.UserToOrganizationPermission;
import server.permissions.results.JoinResult;
import server.repositories.OrganizationMemberRepository;
import server.repositories.OrganizationRepository;

@Controller
@RequestMapping(value = "/organization")
@Transactional
public class OrganizationController extends GroupController<Organization> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  public OrganizationController(FuseSessionController fuseSessionController) {
    super(fuseSessionController);
  }

  @Override
  protected CrudRepository<Organization, Long> getGroupRepository() {
    return organizationRepository;
  }

  @Override
  protected CrudRepository<? extends UserToGroupRelationship, Long> getRelationshipRepository() {
    return organizationMemberRepository;
  }

  @Override
  protected UserToGroupPermission getUserToGroupPermission(User user, Organization group) {
    return permissionFactory.createUserToOrganizationPermission(user, group);
  }

  @Override
  protected void addMember(User user, Organization group) {
    OrganizationMember member = new OrganizationMember();
    member.setUser(user);
    member.setOrganization(group);
    member.setRoleId(DEFAULT_USER);

    organizationMemberRepository.save(member);
  }

  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }


}
