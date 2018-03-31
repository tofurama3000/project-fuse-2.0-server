package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.Link;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.List;

@Data
@MappedSuperclass
public abstract class GroupProfile<T extends Group> {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String headline;

  private String summary;

  private Long thumbnail_id = 0L;

  private Long background_id = 0L;

  private String tags;

  @JsonIgnore
  public abstract T getGroup();

  public abstract void setGroup(T group);

  public abstract List<Link> getLinks();

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
