package example.dto;

import lombok.Getter;

import java.sql.Timestamp;

/**
 * Created by tofurama on 11/8/17.
 */
public class Session {

  public Session(String id, User sessionFor){
    this.email = sessionFor.getEmail();
    this.name = sessionFor.getName();
    this.session = id;
    this.created = new Timestamp(System.currentTimeMillis());
  }

  public Session(String session, User sessionFor, Timestamp created){
    this.email = sessionFor.getEmail();
    this.name = sessionFor.getName();
    this.session = session;
    this.created = created;
  }

  @Getter
  private String email;
  @Getter
  private String name;
  @Getter
  private String session;
  @Getter
  private Timestamp created;

  @Override
  public String toString(){
    return this.session;
  }
}
