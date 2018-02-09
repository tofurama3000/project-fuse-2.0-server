package server.entities.user_to_group.relationships;

import lombok.Setter;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationMember;
import server.entities.dto.user.User;
import server.repositories.group.organization.OrganizationMemberRepository;

import java.util.HashSet;
import java.util.Set;

public class UserToOrganizationRelationship extends UserToGroupRelationship<Organization> {

  @Setter
  private OrganizationMemberRepository organizationMemberRepository;

  public UserToOrganizationRelationship(User user, Organization group) {
    super(user, group);
  }

  @Override
  public boolean addRelationship(int role) {
    Set<Integer> roles = new HashSet<>(organizationMemberRepository.getRoles(group, user));
    if (roles.contains(role)) {
      return false;
    }

    OrganizationMember relationship = new OrganizationMember();
    relationship.setGroup(group);
    relationship.setUser(user);
    relationship.setRoleId(role);

    organizationMemberRepository.save(relationship);
    return true;
  }

  @Override
  public boolean removeRelationship(int role) {
    Set<Integer> roles = new HashSet<>(organizationMemberRepository.getRoles(group, user));
    if (!roles.contains(role)) {
      return false;
    }

    organizationMemberRepository.delete(group, user, role);
    return true;
  }
}
