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
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;
import server.entities.dto.organization.Organization;
import server.entities.dto.organization.OrganizationMember;
import server.entities.dto.project.Project;
import server.permissions.PermissionFactory;
import server.permissions.UserToGroupPermission;
import server.permissions.UserToOrganizationPermission;
import server.permissions.results.JoinResult;
import server.repositories.OrganizationMemberRepository;
import server.repositories.OrganizationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/organization")
@Transactional
public class OrganizationController extends GroupController<Organization> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private FuseSessionController fuseSessionController;

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
  @PutMapping
  @ResponseBody
  public GeneralResponse updateGroup(@RequestBody Organization org, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, DENIED, errors);
    }
    User user = null;
    user.setId(session.get().getUser().getId());
    boolean  permission = getUserToGroupPermission(user, org).canUpdate();
    if(!permission){
      errors.add("Insufficient right");
      return new GeneralResponse(response, DENIED, errors);
    }
    organizationRepository.save(org);
    return new GeneralResponse(response, GeneralResponse.Status.OK);
  }

  @Override
  protected Session getSession() {
    return sessionFactory.openSession();
  }


}
