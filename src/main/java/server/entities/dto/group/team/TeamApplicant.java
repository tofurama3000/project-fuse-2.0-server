package server.entities.dto.group.team;

import lombok.Data;
import server.entities.dto.group.GroupApplicant;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "team_applicant")
public class TeamApplicant extends GroupApplicant<Team> {

  @ManyToOne
  @JoinColumn(name = "team_id", referencedColumnName = "id")
  private Team team;

  @Override
  public Team getGroup() {
    return team;
  }

  @Override
  public void setGroup(Team group) {
    team = group;
  }
}
