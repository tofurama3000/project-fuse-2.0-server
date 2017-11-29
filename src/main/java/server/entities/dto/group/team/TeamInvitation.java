package server.entities.dto.group.team;

import lombok.Data;
import server.entities.dto.group.GroupInvitation;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Entity
@Table(name = "team_invitation")
public class TeamInvitation extends GroupInvitation<Team> {
  @ManyToOne
  @JoinColumn(name = "team_id", referencedColumnName = "id")
  private Team team;

  @Override
  @Transient
  public Team getGroup() {
    return team;
  }

  @Override
  @Transient
  public void setGroup(Team group) {
    team = group;
  }
}
