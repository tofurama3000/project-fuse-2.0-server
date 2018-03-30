package server.entities.dto.user;

import server.entities.dto.group.project.Project;

public class UserProjectCount {
  private User user;
  private int num;
  public  UserProjectCount(User user, int num){
    this.user = user;
    this.num = num;
  }
}
