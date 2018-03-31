package server.entities.dto.user;

import lombok.Data;

@Data
public class UserProjectCount {
  private User user;
  private int num;
  public  UserProjectCount(User user, int num){
    this.user = user;
    this.num = num;
  }
}
