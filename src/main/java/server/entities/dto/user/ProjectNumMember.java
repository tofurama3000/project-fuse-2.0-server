package server.entities.dto.user;

import lombok.Data;
import server.entities.dto.group.project.Project;

@Data
public class ProjectNumMember {
  private Project project;
  private Long num;
  public  ProjectNumMember(Project p, Long num){
    project = p;
    this.num = num;
  }
}
