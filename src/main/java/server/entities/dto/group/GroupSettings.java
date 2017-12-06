package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.group.interview.Interview;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@MappedSuperclass
public abstract class GroupSettings {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @OneToMany
  @JoinColumns({
      @JoinColumn(name = "group_id", referencedColumnName = "group_id"),
      @JoinColumn(name = "group_type", referencedColumnName = "group_type")
  })
  private List<Interview> interviewSlots;

  @JsonIgnore
  @Column(name = "group_type")
  private String groupType;

  public abstract String getGroupType();
}
