package server.entities.dto.group.project;

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
@Table(name = "project_profile")
public class ProjectProfile extends GroupProfile<Project> {

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "project_id", referencedColumnName = "id")
  private Project project;

  @OneToMany
  @JoinColumn(updatable=false,insertable=false, name="referenced_id", referencedColumnName="project_id")
  private List<Link> links;

  private List<Link> getLinks() {
    return links.stream().filter(link -> link.getReferencedType().equals(groupType)).collect(Collectors.toList());
  }

  @JsonIgnore
  @Transient
  private String groupType = "Project";

  @Override
  public Project getGroup() {
    return project;
  }

  @Override
  public void setGroup(Project group) {
    project = group;
  }

}
