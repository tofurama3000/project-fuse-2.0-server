package server.entities.dto;

import lombok.Data;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "sessionId")
@Data
public class Session {

  public Session(String sessionId, User sessionFor) {
    this.email = sessionFor.getEmail();
    this.name = sessionFor.getName();
    this.sessionId = sessionId;
    this.created = new Timestamp(System.currentTimeMillis());
  }

  public Session(String sessionId, User sessionFor, Timestamp created) {
    this.email = sessionFor.getEmail();
    this.name = sessionFor.getName();
    this.sessionId = sessionId;
    this.created = created;
  }

  public Session() {
  }

  @Getter
  @Id
  @Column(name = "session_id")
  private String sessionId;

  @Getter
  private String name;

  @Getter
  private String email;

  @Getter
  private Timestamp created;

  @Override
  public String toString() {
    return this.sessionId;
  }
}
