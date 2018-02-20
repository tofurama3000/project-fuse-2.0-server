package server.entities.dto.group.organization;


import lombok.Data;
import lombok.Getter;
import server.entities.dto.group.GroupMember;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organization_member")
@Data
public class OrganizationMember extends GroupMember<Organization> {

  @ManyToOne
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  @Getter
  private Organization organization;

  @Override
  public void setGroup(Organization group) {
    organization = group;
  }

  @Override
  public Organization getGroup() {
    return organization;
  }
}
