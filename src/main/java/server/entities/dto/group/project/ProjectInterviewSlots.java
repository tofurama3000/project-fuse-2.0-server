package server.entities.dto.group.project;

import lombok.Data;
import server.entities.dto.group.interview.Interview;

import java.util.List;

@Data
public class ProjectInterviewSlots {
  private Project project;
  private List<Interview> interviews;

  public ProjectInterviewSlots(Project project, List<Interview> interviews) {
    this.project = project;
    this.interviews = interviews;
  }
}
