package server.entities.dto.group;

import static server.entities.Restriction.INVITE;
import static server.entities.Restriction.NONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.Interviewable;
import server.entities.Restriction;
import server.entities.dto.User;
import server.entities.dto.group.project.Project;

import javax.persistence.*;

@Data
@MappedSuperclass
public abstract class Group<Profile extends GroupProfile> implements  Interviewable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  private User owner;

  private String name;

  @Column(name = "restriction")
  @JsonIgnore
  private String restrictionString;

  @Transient
  private Boolean canEdit;

  public Restriction getRestriction() {
    if (restrictionString != null && restrictionString.equals("INVITE")) {
      return INVITE;
    }
    return NONE;
  }

  public void setRestriction(String restriction) {
    restrictionString = restriction;
  }

  public abstract Profile getProfile();

  public abstract void setProfile(Profile p);

  @Override
  public String toString() {
    return getGroupType() + " " + getName() + " " + getOwner();
  }
}
