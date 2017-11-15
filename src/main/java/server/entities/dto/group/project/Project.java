package server.entities.dto.group.project;

import server.entities.dto.group.Group;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "project")
public class Project extends Group {
  @Override
  public String getTableName() {
    return "Project";
  }

  @Override
  public String getRelationshipTableName() {
    return "ProjectMember";
  }
}
