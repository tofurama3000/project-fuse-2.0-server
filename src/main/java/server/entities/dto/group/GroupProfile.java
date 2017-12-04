package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public abstract class GroupProfile<T extends Group> {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String headline;

  private String summary;

  @JsonIgnore
  public abstract T getGroup();

  public abstract void setGroup(T group);

  public GroupProfile merge(GroupProfile profileToSave, GroupProfile profile) {
    if (profile.getHeadline() != null) {
      profileToSave.setHeadline(profile.getHeadline());
    }
    if (profile.getSummary() != null) {
      profileToSave.setSummary(profile.getSummary());
    }
    return profileToSave;
  }

}
