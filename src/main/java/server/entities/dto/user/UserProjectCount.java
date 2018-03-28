package server.entities.dto.user;

import server.entities.dto.group.project.Project;

public class UserProjectCount {
  private User user;
  private int num;
  public  UserProjectCount(User u, int num){
    user = u;
    this.num = num;
  }
}
