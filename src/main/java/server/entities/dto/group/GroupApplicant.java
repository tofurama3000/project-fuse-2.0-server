package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.User;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Data
@MappedSuperclass
public abstract class GroupApplicant<T extends Group> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

  @Column(name = "status")
    private String status;

  @Column(name = "time")
  private LocalDateTime time;

  public void setTime(String dateTime)
  {  ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
    time = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }


  @JsonIgnore
  public LocalDateTime getDateTime(){
    return time;
  }

  public String getTime(){
    return time.toString() + "+00:00";
  }

  @JsonIgnore
  public abstract T getGroup();

  public abstract void setGroup(T group);

}
