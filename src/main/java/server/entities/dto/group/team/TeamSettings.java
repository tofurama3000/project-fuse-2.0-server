package server.entities.dto.group.team;

import server.entities.dto.group.GroupSettings;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "team_settings")
public class TeamSettings extends GroupSettings {

  @OneToOne
  @JoinColumn(name = "group_id", referencedColumnName = "id")
  private Team team;

  @Override
  public String getGroupType() {
    return "Team";
  }
}
