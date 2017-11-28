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

  @OneToOne
  @JoinColumn(name = "id", referencedColumnName = "team_profile_id")
  private TeamProfile profile;

  @Override
  public String getTableName() {
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
  public  void setProfile (TeamProfile p){
   profile.setHeadline( p.getHeadline());
   profile.setSummary(p.getSummary());
  }
}
