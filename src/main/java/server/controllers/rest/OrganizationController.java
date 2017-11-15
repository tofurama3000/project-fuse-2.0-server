package server.controllers.rest;

import static server.constants.RoleValue.DEFAULT_USER;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.GroupInvitation;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.organization.Organization;
import server.entities.dto.organization.OrganizationInvitation;
import server.entities.dto.organization.OrganizationMember;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.repositories.UserRepository;
import server.repositories.organization.OrganizationInvitationRepository;
import server.repositories.organization.OrganizationMemberRepository;
import server.repositories.organization.OrganizationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
  private OrganizationInvitationRepository organizationInvitationRepository;

  @Autowired
  private SessionFactory sessionFactory;

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
  protected void addMember(User user, Organization group, int role) {
    OrganizationMember member = new OrganizationMember();
    member.setUser(user);
    member.setOrganization(group);
    member.setRoleId(role);

    organizationMemberRepository.save(member);
  }

  @PostMapping(path = "/invite")
  @ResponseBody
  public GeneralResponse invite(@RequestBody OrganizationInvitation organizationInvitation,
                                HttpServletRequest request, HttpServletResponse response) {
    return generalInvite(organizationInvitation, request, response);
  }

  @Override
  protected void saveInvitation(GroupInvitation<Organization> invitation) {
    organizationInvitationRepository.save((OrganizationInvitation) invitation);
  }

  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }


}
