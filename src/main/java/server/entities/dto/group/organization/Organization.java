package server.entities.dto.group.organization;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import server.entities.dto.group.Group;

import javax.persistence.*;

@ToString(exclude = "profile")
@Entity
@Table(name = "organization")
public class Organization extends Group<OrganizationProfile> {

  @JsonManagedReference
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "organization_profile_id", referencedColumnName = "id")
  private OrganizationProfile profile;

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private OrganizationSettings organizationSettings;

  private boolean canEveryoneCreate;

  @Transient
  @Getter
  @Setter
  private boolean canCreateProject;

  public boolean getCanEveryoneCreate() {
    return canEveryoneCreate;
  }

  public void setCanEveryoneCreate(boolean b) {
    canEveryoneCreate = b;
  }

  @Override
  public String getGroupType() {
    return "Organization";
  }

  @Override
  public OrganizationProfile getProfile() {
    return profile;
  }

  @Override
  public void setProfile(OrganizationProfile p) {
    profile = p;
  }

  public static String esIndex() {
    return "organizations";
  }

  @Override
  public String getEsIndex() {
    return esIndex();
  }
}
