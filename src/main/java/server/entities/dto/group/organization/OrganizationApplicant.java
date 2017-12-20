package server.entities.dto.group.organization;


import lombok.Data;
import server.entities.dto.group.GroupApplicant;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "organization_applicant")
public class OrganizationApplicant extends GroupApplicant<Organization>{

  @ManyToOne
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  private Organization organization;

  @Override
  public Organization getGroup() {
    return organization;
  }

  @Override
  public void setGroup(Organization group) {
    organization=group;
  }
}
