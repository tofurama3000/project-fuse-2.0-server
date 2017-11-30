package server.entities.dto.group.project;

import server.entities.dto.group.Group;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "project")
public class Project extends Group<ProjectProfile> {

  @OneToOne
  @JoinColumn(name = "id", referencedColumnName = "project_profile_id")
  private ProjectProfile profile;

  @Override
  public String getTableName() {
    return "Project";
  }

  @Override
  public String getRelationshipTableName() {
    return "ProjectMember";
  }

  @Override
  public ProjectProfile getProfile() {
    return profile;
  }

  @Override
  public void setProfile(ProjectProfile p) {
    profile.setHeadline(p.getHeadline());
    profile.setSummary(p.getSummary());
  }
}
