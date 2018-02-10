package server.entities.dto.group.organization;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.Link;
import server.entities.dto.group.GroupProfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "organization_profile")
public class OrganizationProfile extends GroupProfile<Organization> {

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  private Organization organization;

  @OneToMany
  @JoinColumn(updatable=false,insertable=false, referencedColumnName="organization_id", name="referenced_id")
  private List<Link> links;

  private List<Link> getLinks() {
    return links.stream().filter(link -> link.getReferencedType().equals(groupType)).collect(Collectors.toList());
  }

  @JsonIgnore
  @Transient
  private String groupType = "Organization";

  @Override
  public Organization getGroup() {
    return organization;
  }

  @Override
  public void setGroup(Organization group) {
    organization = group;
  }
}
