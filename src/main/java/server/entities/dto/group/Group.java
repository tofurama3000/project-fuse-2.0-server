package server.entities.dto.group;

import static server.entities.Restriction.INVITE;
import static server.entities.Restriction.NONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.BaseIndexable;
import server.entities.Interviewable;
import server.entities.Restriction;
import server.entities.dto.user.User;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Data
@MappedSuperclass
public abstract class Group<Profile extends GroupProfile> extends BaseIndexable implements Interviewable {

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
    restrictionString = restriction.toUpperCase();
  }

  public abstract Profile getProfile();

  public abstract void setProfile(Profile p);

  @Override
  public String toString() {
    return getGroupType() + " " + getName() + " " + getOwner();
  }


  @Override
  public Map<String, Object> getEsJson() {
    Map<String, Object> map = new HashMap<>();

    map.put("id", this.getId());
    map.put("name", this.getName());
    map.put("owner", this.getOwner().getName());
    map.put("owner_id", this.getOwner().getId());
    map.put("join_restriction", this.getRestrictionString());
    map.put("summary", this.getProfile().getSummary());
    map.put("headline", this.getProfile().getHeadline());
    map.put("index", this.getEsIndex());

    return map;
  }

  public static String esType() {
    return "info";
  }

  @Override
  public String getEsType() {
    return esType();
  }

  @Override
  public String getEsId() {
    return this.getId().toString();
  }

  // Does nothing, it's just to make JSON deserialization happy
  public void setGroupType(String type) {
  }
}
