package server.controllers.rest.group;

import static server.constants.RoleValue.ADMIN;
import static server.constants.RoleValue.CREATE_PROJECT_IN_ORGANIZATION;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;
import static server.controllers.rest.response.CannedResponse.NO_USER_FOUND;
import static server.utility.JoinPermissionsUtil.genericSetJoinPermissions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.PossibleError;
import server.entities.dto.FuseSession;
import server.entities.dto.group.GroupApplication;
import server.entities.dto.group.GroupProfile;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplication;
import server.entities.dto.group.organization.OrganizationInvitation;
import server.entities.dto.group.organization.OrganizationMember;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
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
import server.repositories.group.project.ProjectRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/organizations")
@Api(tags = "Organizations")
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

  @Autowired
  private ProjectRepository projectRepository;

  @GetMapping("/{id}/can_create_project")
  @ResponseBody
  @ApiOperation("Checks whether or not a user can create a project in the organization")
  public TypedResponse<Boolean> canUserCreateProjectForOrganization(
      @ApiParam("ID of the organization")
      @PathVariable(value = "id") Long id,
      HttpServletRequest request, HttpServletResponse response
  ) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }

    User loggedInUser = session.get().getUser();

    if (id == null) {
      errors.add(INVALID_FIELDS);
      return new TypedResponse<>(response, errors);
    }

    Organization organization = organizationRepository.findOne(id);
    if (organization == null) {
      errors.add(NO_GROUP_FOUND);
      return new TypedResponse<>(response, errors);
    }

    UserToOrganizationPermission loggedInUserPermission = new UserToOrganizationPermission(loggedInUser, organization);
    return new TypedResponse<>(response, OK, errors, loggedInUserPermission.canCreateProjectsInOrganization());
  }

  @GetMapping("/{id}/projects")
  @ResponseBody
  @ApiOperation("Returns all projects associated with an organization")
  public TypedResponse<List<Project>> getOrganizationProjects(
      @ApiParam("Id of the organization")
      @PathVariable(value = "id") Long id,
      HttpServletRequest request, HttpServletResponse response
  ) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }

    User loggedInUser = session.get().getUser();

    if (id == null) {
      errors.add(INVALID_FIELDS);
      return new TypedResponse<>(response, errors);
    }

    Organization organization = organizationRepository.findOne(id);
    if (organization == null) {
      errors.add(NO_GROUP_FOUND);
      return new TypedResponse<>(response, errors);
    }

    return new TypedResponse<>(response, OK, errors,
        organizationRepository.getAllProjectsByOrganization(organization).stream()
            .map(item -> setJoinPermissionsForProject(loggedInUser, item))
            .collect(Collectors.toList())
    );
  }

  @PostMapping("/{id}/grantProjectCreatePermission/{user_id}")
  @ResponseBody
  @ApiOperation("Grants specified user to be able to create projects with in organization")
  public BaseResponse grantUserPermissionToCreateProjectsInOrganization(@ApiParam("ID of the organization")
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

  @ApiOperation("Grants everyone to be able to create projects with in organization")
  @CrossOrigin
  @PutMapping("/{id}/grantEveryoneCreatePermission/{action}")
  @ResponseBody
  public BaseResponse grantEveryonePermissionToCreateProjectsInOrganization(@ApiParam("ID of the organization")
                                                                            @PathVariable(value = "id") Long id,
                                                                            @ApiParam("Action: can be true or false")
                                                                            @PathVariable(value = "action") String action,
                                                                            HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }

    User loggedInUser = session.get().getUser();

    if (id == null) {
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

    organization.setCanEveryoneCreate(action.equals("true"));
    organizationRepository.save(organization);
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
    group.setNumberOfMembers((long) new HashSet<>(organizationMemberRepository.getUsersByGroup(group)).size());
    organizationRepository.save(group);
    group.indexAsync();
  }

  @Override
  protected void addRelationship(User user, Organization group, int role) {
    relationshipFactory.createUserToOrganizationRelationship(user, group).addRelationship(role);
    group.setNumberOfMembers((long) new HashSet<>(organizationMemberRepository.getUsersByGroup(group)).size());
    organizationRepository.save(group);
    group.indexAsync();
  }

  protected void addProjectRelationship(User user, Project group, int role) {
    relationshipFactory.createUserToProjectRelationship(user, group).addRelationship(role);
  }


  @ApiOperation("Grant admin access for a user")
  @PostMapping(path = "/{id}/members/{member_id}/grant/project_create")
  @ResponseBody
  public BaseResponse grantCreateAccess(
      @ApiParam("The id of the group to grant access for")
      @PathVariable(value = "id") Long id,
      @ApiParam("The id of the user to grant access to")
      @PathVariable(value = "member_id") Long memberId,
      HttpServletRequest request, HttpServletResponse response
  ) {
    return grantAccessForMember(id, memberId, CREATE_PROJECT_IN_ORGANIZATION, response, request);
  }

  @ApiOperation("Grant admin access for a user")
  @PostMapping(path = "/{id}/members/{member_id}/revoke/project_create")
  @ResponseBody
  public BaseResponse revokeCreateAccess(
      @ApiParam("The id of the group to grant access for")
      @PathVariable(value = "id") Long id,
      @ApiParam("The id of the user to grant access to")
      @PathVariable(value = "member_id") Long memberId,
      HttpServletRequest request, HttpServletResponse response
  ) {
    return revokeAccessForMember(id, memberId, CREATE_PROJECT_IN_ORGANIZATION, response, request);
  }


  @Override
  protected GroupApplication<Organization> getApplication() {
    return new OrganizationApplication();
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
  protected UserToGroupPermission<Organization> getUserToGroupPermissionTyped(User user, Organization group) {
    return permissionFactory.createUserToOrganizationPermission(user, group);
  }

  private UserToOrganizationPermission getUserToOrganizationPermission(User user, Organization org) {
    return permissionFactory.createUserToOrganizationPermission(user, org);
  }

  @Override
  protected PossibleError validateGroup(User user, Organization group) {
    return new PossibleError(OK);
  }

  @Override
  protected void saveInvitation(OrganizationInvitation invitation) {
    organizationInvitationRepository.save(invitation);
  }

  @Override
  protected Organization setJoinPermissions(User user, Organization group) {
    group = super.setJoinPermissions(user, group);
    UserToOrganizationPermission permission = getUserToOrganizationPermission(user, group);
    group.setCanCreateProject(permission.canCreateProjectsInOrganization());
    return group;
  }

  private Project setJoinPermissionsForProject(User user, Project res) {
    UserToGroupPermission permission = permissionFactory.createUserToProjectPermission(user, res);
    genericSetJoinPermissions(user, res, permission);
    return res;
  }
}
