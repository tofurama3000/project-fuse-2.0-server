package server.entities.dto.group.organization;

import server.entities.dto.group.GroupSettings;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organization_settings")
public class OrganizationSettings extends GroupSettings {
  @OneToOne
  @JoinColumn(name = "group_id", referencedColumnName = "id")
  private Organization organization;

  @Override
  public String getGroupType() {
    return "Organization";
  }
}
