package server.entities.dto.group.organization;

import server.entities.dto.group.Group;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "organization")
public class Organization extends Group {

  @Override
  public String getGroupType() {
    return "Organization";
  }
}
