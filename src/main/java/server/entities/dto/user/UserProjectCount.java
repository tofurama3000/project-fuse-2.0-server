package server.entities.dto.user;

import lombok.Data;

@Data
public class UserProjectCount {
  private User user;
  private int projectCount;

  public UserProjectCount(User user, int projectCount) {
    this.user = user;
    this.projectCount = projectCount;
  }
}
