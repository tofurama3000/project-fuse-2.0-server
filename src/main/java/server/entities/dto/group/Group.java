package server.entities.dto.group;

import static server.entities.Restriction.INVITE;
import static server.entities.Restriction.NONE;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.BaseIndexable;
import server.entities.Interviewable;
import server.entities.Restriction;
import server.entities.dto.user.User;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

  private Long numberOfMembers;

  @Column(name = "restriction")
  @JsonIgnore
  private String restrictionString;

  @Transient
  private Boolean canEdit;

  private Boolean deleted;

  @Transient
  private Boolean canJoin;

  @Transient
  private Boolean canApply;

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
    if (deleted)
      return null;

    Map<String, Object> map = new HashMap<>();

    map.put("id", this.getId());
    map.put("name", this.getName());
    map.put("owner", this.getOwner().getName());
    map.put("owner_id", this.getOwner().getId());
    map.put("join_restriction", this.getRestrictionString());
    map.put("summary", this.getProfile().getSummary());
    map.put("headline", this.getProfile().getHeadline());
    map.put("img", this.getProfile().getThumbnail_id());
    map.put("number_of_members", this.getNumberOfMembers());
    map.put("index", this.getEsIndex());
    map.put("deleted", this.getDeleted());


      Optional<Profile> maybeProfile = Optional.ofNullable(this.getProfile());

    map.put("tags", maybeProfile.map(profile -> getTagsList(profile.getTags()))
        .orElse(new ArrayList<>()));

    return map;
  }

  private List<String> getTagsList(String tagsString) {
    if (tagsString == null) {
      return new ArrayList<>();
    }
    return Arrays.stream(tagsString.split(","))
        .filter(tag -> !tag.isEmpty()).collect(Collectors.toList());
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
