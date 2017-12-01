package server.entities.dto.group.team;

import server.entities.dto.group.Group;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "team")
public class Team extends Group<TeamProfile> {

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private TeamSettings teamSettings;

  @OneToOne
  @JoinColumn(name = "team_profile_id", referencedColumnName = "id")
  private TeamProfile profile;

  @Override
  public String getGroupType() {
    return "Team";
  }

  @Override
  public String getRelationshipTableName() {
    return "TeamMember";
  }

  @Override
  public TeamProfile getProfile() {
    return profile;
  }

  @Override
  public void setProfile(TeamProfile p) {
    profile = p;
  }
}
