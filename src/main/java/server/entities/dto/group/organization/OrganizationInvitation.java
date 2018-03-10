package server.entities.dto.group.organization;

import lombok.Data;
import server.entities.dto.group.GroupApplication;
import server.entities.dto.group.GroupInvitation;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "organization_invitation")
public class OrganizationInvitation extends GroupInvitation<Organization> {

  @ManyToOne
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  private Organization organization;

  @OneToOne
  @JoinColumn(name = "applicant_id", referencedColumnName = "id")
  private OrganizationApplication applicant;

  @Override
  public Organization getGroup() {
    return organization;
  }

  @Override
  public OrganizationApplication getApplicant() {
    return applicant;
  }

  @Override
  public void setApplicant(GroupApplication applicant) {
    this.applicant = (OrganizationApplication) applicant;
  }


  @Override
  public void setGroup(Organization group) {
    organization = group;
  }
}