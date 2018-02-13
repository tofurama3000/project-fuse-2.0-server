package server.entities.dto.group.team;

import lombok.Data;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.project.ProjectApplicant;

import javax.persistence.*;

@Data
@Entity
@Table(name = "team_invitation")
public class TeamInvitation extends GroupInvitation<Team> {
  @ManyToOne
  @JoinColumn(name = "team_id", referencedColumnName = "id")
  private Team team;

  @OneToOne
  @JoinColumn(name = "applicant_id", referencedColumnName = "id")
  private TeamApplicant applicant;

  @Override
  @Transient
  public Team getGroup() {
    return team;
  }

  @Override
  public TeamApplicant getApplicant(){ return applicant; }

  @Override
  public void setApplicant(GroupApplicant applicant) {
    this.applicant.setId(applicant.getId());
    this.applicant.setGroup(team);
    this.applicant.setTeam(team);
    this.applicant.setSender(applicant.getSender());
    this.applicant.setStatus(applicant.getStatus());
    this.applicant.setTime(applicant.getTime());
  }



  @Override
  @Transient
  public void setGroup(Team group) {
    team = group;
  }
}
