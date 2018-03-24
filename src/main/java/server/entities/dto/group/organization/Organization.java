package server.entities.dto.group.organization;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import server.entities.dto.group.Group;
import server.entities.dto.user.User;
import server.handlers.GroupMemberHelper;
import server.handlers.InterviewHelper;
import server.repositories.group.organization.OrganizationMemberRepository;

import javax.persistence.*;
import java.util.*;

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
