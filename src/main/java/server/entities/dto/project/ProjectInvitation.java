package server.entities.dto.project;

import lombok.Data;
import server.entities.dto.GroupInvitation;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "project_invitation")
public class ProjectInvitation extends GroupInvitation<Project> {
  @ManyToOne
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