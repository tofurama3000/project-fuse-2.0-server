package server.entities.dto.group.team;

import server.entities.dto.group.Group;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "team")
public class Team extends Group {

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private TeamSettings teamSettings;

  @Override
  public String getGroupType() {
    return "Team";
  }
}
