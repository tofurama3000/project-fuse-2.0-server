package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.errors.BadDataException;
import server.controllers.rest.errors.DeniedException;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.ProjectNumMember;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.repositories.group.organization.OrganizationRepository;

import java.util.ArrayList;
import java.util.List;

import static server.constants.RoleValue.ADMIN;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.NO_GROUP_FOUND;

@Component
public class GroupMemberHelper {
  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private PermissionFactory permissionFactory;

  public List<ProjectNumMember> usersInEachProject(Long id, User user) throws DeniedException, BadDataException {
    List<ProjectNumMember> list = new ArrayList<>() ;
    Organization organization = organizationRepository.findOne(id);
    if(organization==null)
      throw new BadDataException(NO_GROUP_FOUND);
    UserToOrganizationPermission userToOrganizationPermission = permissionFactory.createUserToOrganizationPermission(user, organization);
    if (!userToOrganizationPermission.hasRole(ADMIN)) {
      throw new DeniedException(INSUFFICIENT_PRIVELAGES);
    }
    List<Project> projects = organizationRepository.getAllProjectsByOrganization(organization);
    for(Project p : projects)
      list.add(new ProjectNumMember(p,p.getNum_members()));
    return list;
  }
}
