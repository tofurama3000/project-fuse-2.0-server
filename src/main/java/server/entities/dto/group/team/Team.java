package server.entities.dto.group.team;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.ToString;
import server.entities.dto.group.Group;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@ToString(exclude = "profile")
@Entity
@Table(name = "team")
@Deprecated
public class Team extends Group<TeamProfile> {

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private TeamSettings teamSettings;

  @JsonManagedReference
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "team_profile_id", referencedColumnName = "id")
  private TeamProfile profile;

  @Override
  public String getGroupType() {
    return "Team";
  }

  @Override
  public TeamProfile getProfile() {
    return profile;
  }

  @Override
  public void setProfile(TeamProfile p) {
    profile = p;
  }

  public static String esIndex() {
    return "teams";
  }

  @Override
  public String getEsIndex() {
    return esIndex();
  }

}
