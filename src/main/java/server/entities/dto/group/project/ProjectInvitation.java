package server.entities.dto.group.project;

import lombok.Data;
import server.entities.dto.group.GroupApplication;
import server.entities.dto.group.GroupInvitation;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "project_invitation")
public class ProjectInvitation extends GroupInvitation<Project> {
  @ManyToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id")
  private Project project;

  @OneToOne
  @JoinColumn(name = "applicant_id", referencedColumnName = "id")
  private ProjectApplication applicant;

  @Override
  public Project getGroup() {
    return project;
  }

  @Override
  public ProjectApplication getApplicant() {
    return applicant;
  }

  @Override
  public void setApplicant(GroupApplication applicant) {
    this.applicant = (ProjectApplication) applicant;
  }

  @Override
  public void setGroup(Project group) {
    project = group;
  }
}