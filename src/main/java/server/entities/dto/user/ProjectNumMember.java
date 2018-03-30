package server.entities.dto.user;

import lombok.Data;
import server.entities.dto.group.project.Project;

@Data
public class ProjectNumMember {
  private Project project;
  private Long num;
  public  ProjectNumMember(Project project, Long num){
    this.project = project;
    this.num = num;
  }
}
