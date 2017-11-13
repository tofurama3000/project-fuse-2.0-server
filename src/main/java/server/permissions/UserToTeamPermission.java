package server.permissions;

import server.entities.dto.User;
import server.entities.dto.team.Team;

public class UserToTeamPermission extends UserToGroupPermission<Team> {

  public UserToTeamPermission(User user, Team group) {
    super(user, group);
  }

  @Override
  public boolean canJoin() {
    return false;
  }
}
