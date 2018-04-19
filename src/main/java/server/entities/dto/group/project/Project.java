package server.entities.dto.group.project;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import server.entities.dto.group.Group;
import server.entities.dto.group.organization.Organization;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@ToString(exclude = "profile")
@Entity
@Table(name = "project")
public class Project extends Group<ProjectProfile> {

  @Getter
  @Setter
  @ManyToOne
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  private Organization organization;

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private ProjectSettings projectSettings;

  @JsonManagedReference
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "project_profile_id", referencedColumnName = "id")
  private ProjectProfile profile;

  @Override
  public String getGroupType() {
    return "Project";
  }

  @Override
  public ProjectProfile getProfile() {
    return profile;
  }

  @Override
  public void setProfile(ProjectProfile p) {
    profile = p;
  }

  public static String esIndex() {
    return "projects";
  }

  @Override
  public String getEsIndex() {
    return esIndex();
  }

  @Override
  public Map<String, Object> getEsJson() {
    Map<String, Object> map = super.getEsJson();
    if (this.getOrganization() != null) {
      Map<String, Object> org = new HashMap<>();
      org.put("id", this.getOrganization().getId());
      org.put("name", this.getOrganization().getName());
      map.put("parent_org", org);
    }

    return map;
  }

}
