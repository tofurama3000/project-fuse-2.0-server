package server.entities.dto.group.organization;

import lombok.Data;
import server.entities.dto.group.GroupApplicant;
import server.entities.dto.group.GroupInvitation;
import server.entities.dto.group.project.ProjectApplicant;

import javax.persistence.*;

@Data
@Entity
@Table(name = "organization_invitation")
public class OrganizationInvitation extends GroupInvitation<Organization> {

  @ManyToOne
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  private Organization organization;

  @OneToOne
  @JoinColumn(name = "applicant_id", referencedColumnName = "id")
  private OrganizationApplicant applicant;

  @Override
  public Organization getGroup() {
    return organization;
  }

  @Override
  public OrganizationApplicant getApplicant() {
    return applicant;
  }

  @Override
  public void setApplicant(GroupApplicant applicant) {
    this.applicant = (OrganizationApplicant) applicant;
  }


  @Override
  public void setGroup(Organization group) {
    organization = group;
  }
}