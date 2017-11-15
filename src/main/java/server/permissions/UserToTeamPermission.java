package server.permissions;

import lombok.Setter;
import org.hibernate.Session;
import server.entities.dto.User;
import server.entities.dto.group.team.Team;
import server.repositories.group.team.TeamMemberRepository;

public class UserToTeamPermission extends UserToGroupPermission<Team> {

  @Setter
  private TeamMemberRepository repository;

  @Setter
  private Session session;

  public UserToTeamPermission(User user, Team group) {
    super(user, group);
  }

  @Override
  protected Session getSession() {
    return session;
  }

  @Override
  protected String getGroupFieldName() {
    return "team";
  }

}
