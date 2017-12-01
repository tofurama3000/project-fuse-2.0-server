package server.entities.dto.group.project;

import lombok.Data;
import server.entities.dto.group.GroupSettings;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "project_settings")
@Data
public class ProjectSettings extends GroupSettings {

  @OneToOne
  @JoinColumn(name = "group_id", referencedColumnName = "id")
  private Project project;

  @Override
  public String getGroupType() {
    return "Project";
  }
}
