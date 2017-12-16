package server.entities.dto.group.team;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.ToString;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupProfile;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@ToString(exclude = "profile")
@Entity
@Table(name = "team")
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

  @Override
  public String getEsIndex() {
    return "team";
  }

}
