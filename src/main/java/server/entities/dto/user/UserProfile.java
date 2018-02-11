package server.entities.dto.user;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import server.entities.dto.Link;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.stream.Collectors;

@ToString(exclude = "user")
@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  private String headline;

  private String summary;

  private String skills;

  @Setter(AccessLevel.NONE)
  @JsonIgnore
  @Transient
  private String profileType = "User";

  @OneToMany
  @JoinColumn(name = "referenced_id", referencedColumnName = "user_id")
  private List<Link> links;

  private List<Link> getLinks() {
    if (links == null) {
      return links;
    }
    return links.stream().filter(link -> link.getReferencedType().equals("User")).collect(Collectors.toList());
  }

  private long thumbnail_id;

  private long background_Id;

  public UserProfile merge(UserProfile original, UserProfile newUserProfile) {
    if (newUserProfile.getHeadline() != null) {
      original.setHeadline(newUserProfile.getHeadline());
    }
    if (newUserProfile.getSummary() != null) {
      original.setSummary(newUserProfile.getSummary());
    }
    if (newUserProfile.getSkills() != null) {
      original.setSkills(newUserProfile.getSkills());
    }
    return original;
  }
}
