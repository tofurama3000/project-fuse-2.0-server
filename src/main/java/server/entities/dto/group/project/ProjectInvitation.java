package server.entities.dto.group.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;

import javax.persistence.*;

@Data
@Entity
@Table(name = "project_invitation")
public class ProjectInvitation extends GroupInvitation<Project> {
  @ManyToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id")
  private Project project;

  @OneToOne
  @JoinColumn(name = "applicant_id", referencedColumnName = "id")
  private ProjectApplicant applicant;

  @Override
  public Project getGroup() {
    return project;
  }

  @Override
  public  ProjectApplicant getApplicant(){ return applicant; }

  @Override
  public void setApplicant(GroupApplicant applicant) {
    this.applicant=(ProjectApplicant) applicant;
  }

  @Override
  public void setGroup(Project group) {
    project = group;
  }
}