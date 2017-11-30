package server.entities.dto.group.project;

import lombok.Data;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "project_profile")
public class ProjectProfile extends GroupProfile<Project> {

  @OneToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id")
  private Project project;

  @Override
  public Project getGroup() {
    return project;
  }

  @Override
  public void setGroup(Project group) {
    project = group;
  }

}
