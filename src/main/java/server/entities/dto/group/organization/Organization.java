package server.entities.dto.group.organization;

import server.entities.dto.group.Group;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organization")
public class Organization extends Group {

  @JoinColumn(name = "id", referencedColumnName = "group_id")
  @OneToOne
  private OrganizationSettings organizationSettings;

  @Override
  public String getGroupType() {
    return "Organization";
  }
}