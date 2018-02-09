package server.controllers.rest.group;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.CREATE_PROJECT_IN_ORGANIZATION;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;
import static server.controllers.rest.response.CannedResponse.NO_USER_FOUND;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.constants.RoleValue;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.CannedResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.PossibleError;
import server.entities.dto.FuseSession;
import server.entities.dto.user.User;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.group.organization.OrganizationMember;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.entities.user_to_group.relationships.RelationshipFactory;
import server.repositories.group.GroupApplicantRepository;
import server.repositories.group.GroupInvitationRepository;
import server.repositories.group.GroupMemberRepository;
import server.repositories.group.GroupRepository;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.organization.OrganizationInvitationRepository;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationProfileRepository;
import server.repositories.group.organization.OrganizationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/organizations")
@Api(tags="Organizations")
@Transactional
@SuppressWarnings("unused")
public class OrganizationController extends GroupController<Organization, OrganizationMember, OrganizationInvitation> {

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private OrganizationApplicantRepository organizationApplicantRepository;

  @Autowired
  private OrganizationProfileRepository organizationProfileRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private OrganizationInvitationRepository organizationInvitationRepository;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private RelationshipFactory relationshipFactory;

  @PostMapping("/{id}/grantProjectCreatePermission/{user_id}")
  @ResponseBody
  @ApiOperation("Grants specified user to be able to create projects with in organization")
  public GeneralResponse grantUserPermissionToCreateProjectsInOrganization(@ApiParam("ID of the organization")
                                                                           @PathVariable(value = "id") Long id,
                                                                           @ApiParam("Id of user to be granted permission")
                                                                           @PathVariable(value = "user_id") Long userId,
                                                                           HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    User loggedInUser = session.get().getUser();

    if (id == null || userId == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, errors);
    }

    Organization organization = organizationRepository.findOne(id);
    if (organization == null) {
      errors.add(NO_GROUP_FOUND);
      return new GeneralResponse(response, errors);
    }

    UserToOrganizationPermission loggedInUserPermission =
        permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);

    if (!loggedInUserPermission.hasRole(ADMIN)) {
      return new GeneralResponse(response, BaseResponse.Status.DENIED, INSUFFICIENT_PRIVELAGES);
    }

    User otherUser = userRepository.findOne(userId);
    if (otherUser == null) {
      return new GeneralResponse(response, BaseResponse.Status.DENIED, NO_USER_FOUND);
    }

    UserToOrganizationPermission otherUserPermission =
        permissionFactory.createUserToOrganizationPermission(otherUser, organization);
    if (!otherUserPermission.isMember()) {
      return new GeneralResponse(response, BaseResponse.Status.DENIED, "User is not a member");
    }

    addRelationship(otherUser, organization, CREATE_PROJECT_IN_ORGANIZATION);
    return new GeneralResponse(response, OK);
  }

  @Override
  protected Organization createGroup() {
    return new Organization();
  }

  @Override
  protected GroupRepository<Organization> getGroupRepository() {
    return organizationRepository;
  }

  @Override
  protected GroupApplicantRepository getGroupApplicantRepository() {
    return organizationApplicantRepository;
  }

  @Override
  protected GroupProfile<Organization> saveProfile(Organization org) {
    return organizationProfileRepository.save(org.getProfile());
  }

  @Override
  protected GroupMemberRepository<Organization, OrganizationMember> getRelationshipRepository() {
    return organizationMemberRepository;
  }

  @Override
  protected UserToGroupPermission getUserToGroupPermission(User user, Organization group) {
    return permissionFactory.createUserToOrganizationPermission(user, group);
  }

  @Override
  protected void removeRelationship(User user, Organization group, int role) {
    relationshipFactory.createUserToOrganizationRelationship(user, group).removeRelationship(role);
  }

  @Override
  protected void addRelationship(User user, Organization group, int role) {
    relationshipFactory.createUserToOrganizationRelationship(user, group).addRelationship(role);
  }

  @Override
  protected GroupApplicant<Organization> getApplication() {
    return new OrganizationApplicant();
  }

  @Override
  protected OrganizationInvitation getInvitation() {
    return new OrganizationInvitation();
  }

  @Override
  protected GroupInvitationRepository<OrganizationInvitation> getGroupInvitationRepository() {
    return organizationInvitationRepository;
  }

  @Override
  protected PossibleError validateGroup(User user, Organization group) {
    return new PossibleError(OK);
  }

  @Override
  protected void saveInvitation(OrganizationInvitation invitation) {
    organizationInvitationRepository.save(invitation);
  }

}
