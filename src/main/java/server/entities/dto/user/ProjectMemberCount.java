package server.entities.dto.user;

import lombok.Data;
import server.entities.dto.group.project.Project;

@Data
public class ProjectMemberCount {
  private Project project;
  private Long memberCount;

  public ProjectMemberCount(Project project, Long memberCount) {
    this.project = project;
    this.memberCount = memberCount;
  }
}
