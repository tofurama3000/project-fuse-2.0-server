package server.entities.dto.group.organization;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "organization_profile")
public class OrganizationProfile extends GroupProfile<Organization> {

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  private Organization organization;

  @Override
  public Organization getGroup() {
    return organization;
  }

  @Override
  public void setGroup(Organization group) {
    organization = group;
  }
}
