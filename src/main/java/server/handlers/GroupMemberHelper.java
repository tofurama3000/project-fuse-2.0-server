package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.errors.BadDataException;
import server.controllers.rest.errors.DeniedException;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.statistics.MemberProjectOrganizationInterviewSummaryView;
import server.entities.dto.statistics.UsersWithInvalidProfilesBreakdownView;
import server.entities.dto.statistics.UsersWithInvalidProfilesSummaryView;
import server.entities.dto.user.ProjectMemberCount;
import server.entities.dto.user.User;
import server.entities.dto.user.UserProjectCount;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.repositories.group.organization.OrganizationMemberRepository;
import server.repositories.group.organization.OrganizationRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static server.constants.RoleValue.ADMIN;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;

@Component
public class GroupMemberHelper {
  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private OrganizationMemberRepository organizationMemberRepository;

  @Autowired
  private PermissionFactory permissionFactory;

  public List<ProjectMemberCount> organizationProjectsUserCount(Long organizationId, User loggedInUser) throws DeniedException, BadDataException {
    List<ProjectMemberCount> list = new ArrayList<>();
    Organization organization = organizationRepository.findOne(organizationId);
    if (organization == null)
      throw new BadDataException(NO_GROUP_FOUND);
    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }
    return organizationRepository.getAllProjectsByOrganization(organization).stream().map(project -> new ProjectMemberCount(project, project.getNumberOfMembers())).collect(Collectors.toList());
  }


  public List<UserProjectCount> organizationMembersProjectCount(Long organizationId, User loggedInUser) throws DeniedException, BadDataException {
    Organization organization = organizationRepository.findOne(organizationId);
    if (organization == null)
      throw new BadDataException(NO_GROUP_FOUND);
    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }
    Set<User> users = new HashSet<>(organizationMemberRepository.getUsersByGroup(organization));
    return users.stream().map(user -> new UserProjectCount(user, numberProjectsUserIsIn(user, organization))).collect(Collectors.toList());
  }

  private int numberProjectsUserIsIn(User user, Organization organization) {
    List<Project> projects = organizationRepository.getAllProjectsByOrganization(organization);
    return (int) projects.stream().map(project -> permissionFactory.createUserToProjectPermission(user, project))
        .filter(UserToGroupPermission::isMember).count();
  }

  public  List<MemberProjectOrganizationInterviewSummaryView> getMemberProjectOrganizationInterviewSummaryView(Long organizationId, User loggedInUser) throws BadDataException, DeniedException {
    Organization organization = organizationRepository.findOne(organizationId);
    if (organization == null)
      throw new BadDataException(NO_GROUP_FOUND);
    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }
    return  organizationRepository.getMemberProjectOrganizationInterviewSummary(organizationId);
  }

  public  List<UsersWithInvalidProfilesSummaryView> getUsersWithInvalidProfilesSummaryView(Long organizationId, User loggedInUser) throws BadDataException, DeniedException {
    Organization organization = organizationRepository.findOne(organizationId);
    if (organization == null)
      throw new BadDataException(NO_GROUP_FOUND);
    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }
    return  organizationRepository.getUsersWithInvalidProfilesSummary(organizationId);
  }

  public  List<UsersWithInvalidProfilesBreakdownView> getUsersWithInvalidProfilesBreakdownView(Long organizationId, User loggedInUser) throws BadDataException, DeniedException {
    Organization organization = organizationRepository.findOne(organizationId);
    if (organization == null)
      throw new BadDataException(NO_GROUP_FOUND);
    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }
    return  organizationRepository.getUsersWithInvalidProfilesBreakdown(organizationId);
  }
}
