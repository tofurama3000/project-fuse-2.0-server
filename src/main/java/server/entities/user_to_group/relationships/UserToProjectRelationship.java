package server.entities.user_to_group.relationships;

import lombok.Setter;
import server.entities.dto.User;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.project.ProjectMember;
import server.repositories.group.project.ProjectMemberRepository;

import java.util.HashSet;
import java.util.Set;

public class UserToProjectRelationship extends UserToGroupRelationship<Project> {

  @Setter
  private ProjectMemberRepository projectMemberRepository;

  public UserToProjectRelationship(User user, Project group) {
    super(user, group);
  }

  @Override
  public boolean addRelationship(int role) {
    Set<Integer> roles = new HashSet<>(projectMemberRepository.getRoles(group, user));
    if (roles.contains(role)) {
      return false;
    }

    ProjectMember relationship = new ProjectMember();
    relationship.setGroup(group);
    relationship.setUser(user);
    relationship.setRoleId(role);

    projectMemberRepository.save(relationship);
    return true;
  }

  @Override
  public boolean removeRelationship(int role) {
    Set<Integer> roles = new HashSet<>(projectMemberRepository.getRoles(group, user));
    if (!roles.contains(role)) {
      return false;
    }

    projectMemberRepository.delete(group, user, role);
    return true;
  }
}
