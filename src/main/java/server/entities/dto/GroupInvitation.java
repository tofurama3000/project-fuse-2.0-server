package server.entities.dto;

import lombok.Data;
import server.entities.Group;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public abstract class GroupInvitation<T extends Group> {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private User receiver;

  private User sender;

  private String status;

  public abstract T getGroup();

  public abstract void setGroup(T group);
}
