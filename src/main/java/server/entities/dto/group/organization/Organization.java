package server.entities.dto.group.organization;

import server.entities.dto.group.Group;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organization")
public class Organization extends Group<OrganizationProfile> {
  @OneToOne
  @JoinColumn(name = "id", referencedColumnName = "organization_profile_id")
  private OrganizationProfile profile;

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private OrganizationSettings organizationSettings;

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
    profile.setHeadline(p.getHeadline());
    profile.setSummary(p.getSummary());
  }
}
