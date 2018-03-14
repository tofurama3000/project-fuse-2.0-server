package server.entities.dto.user;

import lombok.Data;
import server.entities.dto.group.interview.Interview;

import java.util.List;

@Data
public class UserInterviewSlots {
  private User user;
  private List<Interview> interviews;

  public UserInterviewSlots(User user, List<Interview> interviews) {
    this.user = user;
    this.interviews = interviews;
  }
}
