package server.entities.dto.group.team;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Entity
@Table(name = "team_profile")
public class TeamProfile extends GroupProfile<Team> {

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "team_id", referencedColumnName = "id")
  private Team team;


  @Override
  @Transient
  public Team getGroup() {
    return team;
  }

  @Override
  @Transient
  public void setGroup(Team group) {
    team = group;
  }


}
