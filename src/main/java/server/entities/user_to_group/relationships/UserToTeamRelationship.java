package server.entities.user_to_group.relationships;

import lombok.Setter;
import server.entities.dto.user.User;
import server.entities.dto.group.team.Team;
import server.entities.dto.group.team.TeamMember;
import server.repositories.group.team.TeamMemberRepository;

import java.util.HashSet;
import java.util.Set;

public class UserToTeamRelationship extends UserToGroupRelationship<Team> {

  @Setter
  private TeamMemberRepository teamMemberRepository;

  public UserToTeamRelationship(User user, Team group) {
    super(user, group);
  }

  @Override
  public boolean addRelationship(int role) {
    Set<Integer> roles = new HashSet<>(teamMemberRepository.getRoles(group, user));
    if (roles.contains(role)) {
      return false;
    }

    TeamMember relationship = new TeamMember();
    relationship.setGroup(group);
    relationship.setUser(user);
    relationship.setRoleId(role);

    teamMemberRepository.save(relationship);
    return true;
  }

  @Override
  public boolean removeRelationship(int role) {
    Set<Integer> roles = new HashSet<>(teamMemberRepository.getRoles(group, user));
    if (!roles.contains(role)) {
      return false;
    }

    teamMemberRepository.delete(group, user, role);
    return true;
  }
}
