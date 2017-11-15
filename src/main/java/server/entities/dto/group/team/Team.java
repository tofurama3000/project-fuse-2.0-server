package server.entities.dto.group.team;

import server.entities.Group;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "team")
public class Team extends Group {
  @Override
  public String getTableName() {
    return "Team";
  }

  @Override
  public String getRelationshipTableName() {
    return "TeamMember";
  }
}
